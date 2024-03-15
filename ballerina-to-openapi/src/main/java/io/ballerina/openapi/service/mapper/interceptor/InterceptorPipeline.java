/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.interceptor.Interceptor.InterceptorType;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.INTERCEPTOR;

/**
 * This {@link InterceptorPipeline} class represents the interceptor pipeline. This class is responsible for building
 * the interceptor pipeline and extracting the effective return types from the pipeline.
 *
 * @since 1.9.0
 */
public class InterceptorPipeline {

    private Interceptor initReqInterceptor = null;
    private Interceptor initResInterceptor = null;
    private final SemanticModel semanticModel;

    private InterceptorPipeline(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public static InterceptorPipeline build(ServiceDeclarationNode serviceDefinition, AdditionalData additionalData) {
        List<Interceptor> interceptors = buildInterceptors(serviceDefinition, additionalData);

        if (!interceptors.isEmpty()) {
            InterceptorPipeline pipeline = new InterceptorPipeline(additionalData.semanticModel());
            Interceptor prevInterceptor = interceptors.get(0);
            if (prevInterceptor.getType().equals(InterceptorType.REQUEST)) {
                pipeline.initReqInterceptor = prevInterceptor;
            } else if (prevInterceptor instanceof ResponseInterceptor) {
                pipeline.initResInterceptor = prevInterceptor;
            }
            for (int i = 1; i < interceptors.size(); i++) {
                prevInterceptor.setNextInReqPath(interceptors.get(i));
                interceptors.get(i).setNextInResPath(prevInterceptor);
                prevInterceptor = interceptors.get(i);
                if (prevInterceptor.getType().equals(InterceptorType.REQUEST) &&
                        Objects.isNull(pipeline.initReqInterceptor)) {
                    pipeline.initReqInterceptor = prevInterceptor;
                } else if (prevInterceptor instanceof ResponseInterceptor) {
                    pipeline.initResInterceptor = prevInterceptor;
                }
            }
            return pipeline;
        }
        return null;
    }

    private static List<Interceptor> buildInterceptors(ServiceDeclarationNode serviceDefinition,
                                                       AdditionalData additionalData) {
        SemanticModel semanticModel = additionalData.semanticModel();
        ModuleMemberVisitor moduleMemberVisitor = additionalData.moduleMemberVisitor();
        Optional<Symbol> optServiceSymbol = semanticModel.symbol(serviceDefinition);
        if (optServiceSymbol.isEmpty() ||
                !(optServiceSymbol.get() instanceof ServiceDeclarationSymbol serviceSymbol)) {
            return new ArrayList<>();
        }

        Optional<TypeSymbol> optInterceptorReturn = serviceSymbol.methods().get("createInterceptors").
                typeDescriptor().returnTypeDescriptor();
        if (optInterceptorReturn.isEmpty()) {
            return new ArrayList<>();
        }

        TypeSymbol interceptorReturn = optInterceptorReturn.get();
        try {
            if (interceptorReturn instanceof TypeReferenceTypeSymbol interceptorType &&
                    isSubTypeOf(interceptorReturn, INTERCEPTOR, semanticModel)) {
                Interceptor.InterceptorType type = getInterceptorType(interceptorType, semanticModel);
                Interceptor interceptor = getInterceptor(interceptorType, type, semanticModel, moduleMemberVisitor);
                return List.of(interceptor);
            } else if (interceptorReturn instanceof TupleTypeSymbol interceptorTupleType) {
                return getInterceptorListFromReturnType(interceptorTupleType, semanticModel, moduleMemberVisitor);
            }
        } catch (InterceptorMapperException e) {
            addWarningDiagnostic(additionalData, e.getMessage());
            return new ArrayList<>();
        }

        String cause = "the return type of `createInterceptors` function should be defined as a tuple with specific" +
                " interceptor types as members. For example: `[ResponseInterceptor_, RequestInterceptor_, " +
                "RequestErrorInterceptor_]`";
        addWarningDiagnostic(additionalData, cause);
        return new ArrayList<>();
    }

    private static void addWarningDiagnostic(AdditionalData additionalData, String cause) {
        ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_126, cause);
        additionalData.diagnostics().add(error);
    }

    private static Interceptor getInterceptor(TypeReferenceTypeSymbol interceptorType, InterceptorType type,
                                              SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor)
            throws InterceptorMapperException {
        return switch (Objects.requireNonNull(type)) {
            case REQUEST -> new RequestInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
            case REQUEST_ERROR -> new RequestErrorInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
            case RESPONSE -> new ResponseInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
            default -> // RESPONSE_ERROR
                    new ResponseErrorInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
        };
    }

    private static List<Interceptor> getInterceptorListFromReturnType(TupleTypeSymbol interceptorTupleType,
                                                                      SemanticModel semanticModel,
                                                                      ModuleMemberVisitor moduleMemberVisitor)
            throws InterceptorMapperException {
        List<Interceptor> interceptors = new ArrayList<>();
        for (TypeSymbol typeDescriptor : interceptorTupleType.memberTypeDescriptors()) {
            if (typeDescriptor instanceof TypeReferenceTypeSymbol interceptorType) {
                InterceptorType type = getInterceptorType(interceptorType, semanticModel);
                Interceptor interceptor = getInterceptor(interceptorType, type, semanticModel,
                        moduleMemberVisitor);
                interceptors.add(interceptor);
            }
        }
        return interceptors;
    }

    private static InterceptorType getInterceptorType(TypeSymbol interceptorType, SemanticModel semanticModel) {
        if (isSubTypeOf(interceptorType, "RequestInterceptor", semanticModel)) {
            return InterceptorType.REQUEST;
        } else if (isSubTypeOf(interceptorType, "RequestErrorInterceptor", semanticModel)) {
            return InterceptorType.REQUEST_ERROR;
        } else if (isSubTypeOf(interceptorType, "ResponseInterceptor", semanticModel)) {
            return InterceptorType.RESPONSE;
        } else { // ResponseErrorInterceptor
            return InterceptorType.RESPONSE_ERROR;
        }
    }

    private static boolean isSubTypeOf(TypeSymbol typeSymbol, String typeName, SemanticModel semanticModel) {
        Optional<Symbol> optType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY, typeName);
        if (optType.isEmpty() || !(optType.get() instanceof TypeDefinitionSymbol typeDef)) {
            return false;
        }
        return typeSymbol.subtypeOf(typeDef.typeDescriptor());
    }

    public InfoFromInterceptors getEffectiveReturnType(ResourceMethodSymbol targetResource, boolean hasDataBinding) {
        return getReturnTypes(targetResource, hasDataBinding);
    }

    private InfoFromInterceptors getReturnTypes(ResourceMethodSymbol targetResource, boolean hasDataBinding) {
        InfoFromInterceptors infoFromInterceptors = new InfoFromInterceptors();
        TargetResource target = new TargetResource(targetResource, hasDataBinding, semanticModel);
        if (Objects.isNull(initReqInterceptor)) {
            updateReturnTypeForTarget(infoFromInterceptors, target);
        } else {
            updateReturnTypeForReqInterceptor(initReqInterceptor, infoFromInterceptors, target);
        }
        return infoFromInterceptors;
    }

    private void updateReturnTypeForReqInterceptor(Interceptor interceptor, InfoFromInterceptors infoFromInterceptors,
                                                   TargetResource targetResource) {
        if (Objects.isNull(interceptor)) {
            updateReturnTypeForTarget(infoFromInterceptors, targetResource);
            return;
        }

        if (interceptor.isContinueExecution() || !interceptor.isInvokable(targetResource)) {
            updateReturnTypeForReqInterceptor(interceptor.getNextInReqPath(), infoFromInterceptors, targetResource);
            if (!interceptor.isInvokable(targetResource)) {
                return;
            }
        }

        if (interceptor.hasErrorReturn()) {
            updateErrorReturnTypeInReqPath(interceptor, infoFromInterceptors, targetResource);
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        updateNonErrorReturnTypeForInterceptor(interceptor, nonErrorReturnType, infoFromInterceptors, false);
    }

    private void updateErrorReturnTypeInReqPath(Interceptor interceptor, InfoFromInterceptors infoFromInterceptors,
                                                TargetResource targetResource) {
        Interceptor nextErrorInterceptor = interceptor.getNextInReqErrorPath();
        if (Objects.nonNull(nextErrorInterceptor) &&
                nextErrorInterceptor.isInvokable(targetResource)) {
            updateReturnTypeForReqInterceptor(nextErrorInterceptor, infoFromInterceptors, targetResource);
        } else {
            nextErrorInterceptor = getNextErrorInterceptor();
            updateErrorReturnTypeForInterceptor(interceptor, infoFromInterceptors, nextErrorInterceptor);
        }
    }

    private Interceptor getNextErrorInterceptor() {
        Interceptor nextErrorInterceptor = null;
        if (Objects.nonNull(initResInterceptor)) {
            if (initResInterceptor.getType().equals(InterceptorType.RESPONSE_ERROR)) {
                nextErrorInterceptor = initResInterceptor;
            } else {
                nextErrorInterceptor = initResInterceptor.getNextInResErrorPath();
            }
        }
        return nextErrorInterceptor;
    }

    private void updateErrorReturnTypeForInterceptor(Interceptor interceptor, InfoFromInterceptors infoFromInterceptors,
                                                     Interceptor nextErrorInterceptor) {
        if (Objects.nonNull(nextErrorInterceptor)) {
            updateReturnTypeForResInterceptor(nextErrorInterceptor, infoFromInterceptors, null, false);
        } else {
            infoFromInterceptors.addReturnTypeFromInterceptors(interceptor.getErrorReturnType());
        }
    }

    private void updateNonErrorReturnTypeForInterceptor(Interceptor interceptor, TypeSymbol nonErrorReturnType,
                                                        InfoFromInterceptors infoFromInterceptors, boolean fromTarget) {
        if (Objects.nonNull(nonErrorReturnType)) {
            Interceptor nextResInterceptor = interceptor.getNextInResPath();
            if (Objects.nonNull(nextResInterceptor)) {
                updateReturnTypeForResInterceptor(nextResInterceptor, infoFromInterceptors, nonErrorReturnType,
                        fromTarget);
            } else {
                updateReturnType(infoFromInterceptors, nonErrorReturnType, fromTarget);
            }
        }
    }

    private void updateReturnTypeForResInterceptor(Interceptor interceptor, InfoFromInterceptors infoFromInterceptors,
                                                   TypeSymbol prevReturnType, boolean fromTarget) {
        if (interceptor.isContinueExecution() && Objects.nonNull(prevReturnType)) {
            updateNonErrorReturnTypeForInterceptor(interceptor, prevReturnType, infoFromInterceptors, fromTarget);
        }

        if (interceptor.hasErrorReturn()) {
            Interceptor nextErrorInterceptor = interceptor.getNextInResErrorPath();
            updateErrorReturnTypeForInterceptor(interceptor, infoFromInterceptors, nextErrorInterceptor);
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        updateNonErrorReturnTypeForInterceptor(interceptor, nonErrorReturnType, infoFromInterceptors, false);
    }

    private static void updateReturnType(InfoFromInterceptors infoFromInterceptors, TypeSymbol prevReturnType,
                                         boolean fromTarget) {
        if (fromTarget) {
            infoFromInterceptors.addReturnTypeFromTargetResource(prevReturnType);
        } else {
            infoFromInterceptors.addReturnTypeFromInterceptors(prevReturnType);
        }
    }

    private void updateReturnTypeForTarget(InfoFromInterceptors infoFromInterceptors, TargetResource targetResource) {
        TypeSymbol returnType = targetResource.getEffectiveReturnType();
        if (Objects.isNull(initResInterceptor)) {
            infoFromInterceptors.addReturnTypeFromTargetResource(returnType);
            return;
        }
        if (targetResource.hasErrorReturn()) {
            updateErrorReturnTypeForTarget(infoFromInterceptors, targetResource);
        }
        TypeSymbol nonErrorReturnType = targetResource.getNonErrorReturnType();
        if (Objects.nonNull(nonErrorReturnType)) {
            updateNonErrorReturnTypeForTarget(infoFromInterceptors, nonErrorReturnType);
        }
    }

    private void updateNonErrorReturnTypeForTarget(InfoFromInterceptors infoFromInterceptors,
                                                   TypeSymbol nonErrorReturnType) {
        if (initResInterceptor.getType().equals(InterceptorType.RESPONSE)) {
            updateReturnTypeForResInterceptor(initResInterceptor, infoFromInterceptors, nonErrorReturnType, true);
        } else {
            Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResPath();
            if (Objects.nonNull(nextResponseErrorInterceptor)) {
                updateReturnTypeForResInterceptor(nextResponseErrorInterceptor, infoFromInterceptors,
                        nonErrorReturnType, true);
            } else {
                infoFromInterceptors.addReturnTypeFromTargetResource(nonErrorReturnType);
            }
        }
    }

    private void updateErrorReturnTypeForTarget(InfoFromInterceptors infoFromInterceptors, TargetResource target) {
        if (initResInterceptor.getType().equals(InterceptorType.RESPONSE_ERROR)) {
            infoFromInterceptors.markErrorsHandledByInterceptors();
            updateReturnTypeForResInterceptor(initResInterceptor, infoFromInterceptors, null, true);
        } else {
            Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResErrorPath();
            if (Objects.nonNull(nextResponseErrorInterceptor)) {
                infoFromInterceptors.markErrorsHandledByInterceptors();
                updateReturnTypeForResInterceptor(nextResponseErrorInterceptor, infoFromInterceptors, null, true);
            } else if (Objects.nonNull(target.getErrorReturnType())) {
                infoFromInterceptors.addReturnTypeFromTargetResource(target.getErrorReturnType());
            }
        }
    }
}
