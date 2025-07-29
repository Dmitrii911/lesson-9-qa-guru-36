package tests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.xlstest.XLS.containsText;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilesParsingTest {

    private ClassLoader cl = FilesParsingTest.class.getClassLoader();

    @DisplayName("Проверка содержимого ZIP-архива")
    @Test
    void zipFileParsingTest() throws Exception {
        try (ZipInputStream zipInput = new ZipInputStream(
                cl.getResourceAsStream("zip.zip"),
                Charset.forName("windows-1251")
        )) {
            ZipEntry entry;

            while ((entry = zipInput.getNextEntry()) != null) {
                System.out.println(entry.getName());
            }
            zipInput.closeEntry();
        }
    }

    @DisplayName("Проверка содержимого CSV-файла из ZIP-архива")
    @Test
    void readFileCsvFromArchive() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("zip.zip"),
                Charset.forName("windows-1251"));
        ) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".csv")) {
                    try (CSVReader csvReader = new CSVReader(new InputStreamReader(zis))) {
                        List<String[]> data = csvReader.readAll();
                        Assertions.assertEquals(4, data.size());
                        Assertions.assertArrayEquals(
                                new String[]{"\uFEFFВалера", " developer", " разработчик"},
                                data.get(0)
                        );
                        Assertions.assertArrayEquals(
                                new String[]{"Дмитрий", " QA", " тестировщик"},
                                data.get(1)
                        );
                        Assertions.assertArrayEquals(
                                new String[]{"Алина", " devops", " девопс"},
                                data.get(2)
                        );
                        Assertions.assertArrayEquals(
                                new String[]{"Игнат", " менеджер", " экономист "},
                                data.get(3)
                        );
                    }
                    break;
                }
            }
        }
    }


    @DisplayName("Проверка содержимого pdf-файла из ZIP архива")
    @Test
    void readFilePdfFromArchive() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("zip.zip"), Charset.forName("windows-1251"));
        ) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".pdf")) {
                    PDF pdf = new PDF(zis);
                    Assertions.assertEquals("pdftk 2.01 - www.pdftk.com", pdf.creator);
                    break;
                }
                zis.closeEntry();
            }
        }
    }

    @DisplayName("Проверка содержимого xls-файла из ZIP архива")
    @Test
    void xlsCheckFileTest() throws Exception {
        try (ZipInputStream zipInput = new ZipInputStream(
                cl.getResourceAsStream("zip.zip"), Charset.forName("windows-1251")
        )) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xlsx")) {
                    XLS xls = new XLS(zipInput);
                    String actualValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                    assertThat(xls,containsText("Auth Service With Credentials"));
                }
            }
        }
    }
}
