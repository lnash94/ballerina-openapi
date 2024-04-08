package io.ballerina.openapi.core.generators.service;

import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;

public class ServiceGenerationHandler {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public List<GenSrcFile> generateServiceFiles(OASServiceMetadata oasServiceMetadata) throws
            FormatterException, BallerinaOpenApiException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String mainContent;
        ServiceDeclarationGenerator serviceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        mainContent = Formatter.format(serviceGenerator.generateSyntaxTree()).toSourceCode();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                oasServiceMetadata.getSrcFile(),
                (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                        oasServiceMetadata.getLicenseHeader()) + mainContent));
        String typeContent = Formatter.format(TypeHandler.getInstance().generateTypeSyntaxTree()).toSourceCode();
        if (!typeContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "types.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + typeContent));
        }

        if (oasServiceMetadata.isServiceTypeRequired()) {
            ServiceTypeGenerator serviceTypeGenerator = new ServiceTypeGenerator(oasServiceMetadata,
                    serviceGenerator.getFunctionsList());
            String serviceType = Formatter.format(serviceTypeGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
        }
        diagnostics.addAll(serviceGenerator.getDiagnostics());
        return sourceFiles;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
