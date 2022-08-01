package uk.gov.hmcts.reform.iahearingsapi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CftLibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
        createCcdRoles(lib);
        createIdamUsers(lib);
        importDefinitions(lib);
    }

    private void importDefinitions(CFTLib lib) throws IOException {
        var ccd_defs_xlsx = Files.readAllBytes(Path.of("resources/ccd-bail-config-dev.xlsx"));
        lib.importDefinition(ccd_defs_xlsx);
    }

    private void createCcdRoles(CFTLib lib) {
        lib.createRoles(
            "caseworker-probate",
            "caseworker-probate-solicitor"
        );
    }

    private void createIdamUsers(CFTLib lib) {
        lib.createIdamUser("test@mailinator.com", "caseworker-probate");
    }
}