package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DocumentWithMetadataTest {

    private final Document document = mock(Document.class);
    private final String description = "Some evidence";
    private final String dateUploaded = "2021-12-25";
    private final DocumentTag tag = DocumentTag.BAIL_SUBMISSION;

    private DocumentWithMetadata documentWithMetadata =
        new DocumentWithMetadata(
            document,
            description,
            dateUploaded,
            tag
        );

    @Test
    void should_hold_onto_values() {

        assertEquals(document, documentWithMetadata.getDocument());
        assertEquals(description, documentWithMetadata.getDescription());
        assertEquals(dateUploaded, documentWithMetadata.getDateUploaded());
        assertEquals(tag, documentWithMetadata.getTag());
    }

}
