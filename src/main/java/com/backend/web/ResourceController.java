package com.backend.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.Profile;
import com.backend.utils.CommonUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/resources")
@Slf4j
public class ResourceController {

    /**
     * this controller just for testing
     */

    public static final String PROFILE = "Profile";

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "user", method = RequestMethod.GET)
    public String helloUser() {
        return "hello user";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "admin", method = RequestMethod.GET)
    public String helloAdmin() {
        return "hello admin";
    }

    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @RequestMapping(value = "client", method = RequestMethod.GET)
    public String helloClient() {
        return "hello user authenticated by normal client";
    }

    @PreAuthorize("hasRole('ROLE_TRUSTED_CLIENT')")
    @RequestMapping(value = "trusted_client", method = RequestMethod.GET)
    public String helloTrustedClient() {
        return "hello user authenticated by trusted client";
    }

    @RequestMapping(value = "principal", method = RequestMethod.GET)
    public Object getPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal;
    }

    @RequestMapping(value = "roles", method = RequestMethod.GET)
    public Object getRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "schema/profile", method = RequestMethod.POST)
    public ResponseEntity<JsonSchema> schema(HttpServletRequest request, HttpServletResponse response)
            throws JsonMappingException {
        return new ResponseEntity<>(CommonUtils.getSchema(Profile.class), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "import/csv", method = RequestMethod.POST)
    public ResponseEntity<List<Profile>> importCsvFile(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return new ResponseEntity<>(readWithCsvToBean("profile.csv"), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "export/excel", method = RequestMethod.GET)
    public ResponseEntity<List<Profile>> exportToExcel(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        List<Profile> profiles = readWithCsvToBean("profile.csv");

        // change the file name
        response.setHeader("Content-Disposition", "attachment; filename=\"profile.xlsx\"");
        XSSFWorkbook workbook = new XSSFWorkbook();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);

        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        Sheet sheet = workbook.createSheet("Profile");
        sheet.setDefaultColumnWidth(30);
        Map<String, JsonSchema> map = CommonUtils.getSchemaProperties(Profile.class);
        List<String> properties = map.keySet().stream().collect(Collectors.toList());

        // create header row
        Row header = sheet.createRow(0);
        for(int i = 0; i < properties.size(); i++) {
            header.createCell(i).setCellValue(properties.get(i));
            header.getCell(i).setCellStyle(style);
        }

        for(int i = 0; i < profiles.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Class<?> c = profiles.get(i).getClass();
            for(String fieldName : properties) {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                log.info(field.get(profiles.get(i)).toString());
                row.createCell(properties.indexOf(fieldName)).setCellValue(field.get(profiles.get(i)).toString());
            }
            log.info("===============================");
        }

        response.setContentType("application/ms-excel");
        response.setHeader("Content-Disposition", "inline; filename=" + "Profile.xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        sheet.getWorkbook().write(outputStream);
        outputStream.flush();
        workbook.close();
        outputStream.close();
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "barcode", method = RequestMethod.GET)
    public ResponseEntity<Void> getBarcode(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("barcode") String barcode) throws Exception {
        AbstractBarcodeBean bean = new Code128Bean();
        bean.setHeight(10d);
        bean.doQuietZone(false);

        OutputStream out = new java.io.FileOutputStream(new File("output.png"));
        BitmapCanvasProvider provider = new BitmapCanvasProvider(out, "image/x-png", 110, BufferedImage.TYPE_BYTE_GRAY, false, 0);

        bean.generateBarcode(provider, barcode);
        provider.finish();

        BufferedImage barcodeImage = provider.getBufferedImage();
        response.setContentType("image/x-png");
        OutputStream outputStream = response.getOutputStream();
        ImageIO.write(barcodeImage, "png", outputStream);
        outputStream.close();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<Profile> readWithCsvReader(String filename) throws IOException {
        // Tutorial : http://www.journaldev.com/12014/opencsv-csvreader-csvwriter-example
        List<Profile> profiles = new ArrayList<>();
        File file = new ClassPathResource(filename).getFile();
        FileInputStream fis = new FileInputStream(file);
        CSVReader reader = new CSVReader(new InputStreamReader(fis), '\t');
        String[] nextLine;
        reader.readNext();
        while ((nextLine = reader.readNext()) != null) {
            Profile profile = new Profile(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[5], nextLine[6]);
            profiles.add(profile);
        }
        reader.close();
        fis.close();
        return profiles;
    }

    public List<Profile> readWithCsvToBean(String filename) throws IOException {
        File file = new ClassPathResource(filename).getFile();
        FileInputStream fis = new FileInputStream(file);
        CSVReader reader = new CSVReader(new InputStreamReader(fis), '\t', ',', 1);
        ColumnPositionMappingStrategy<Profile> beanStrategy = new ColumnPositionMappingStrategy<Profile>();
        beanStrategy.setType(Profile.class);
        beanStrategy.setColumnMapping(new String[]{"ProfileId", "Cardholdername", "Email", "CardNumber", "Status", "Photo"});
        CsvToBean<Profile> csvToBean = new CsvToBean<Profile>();
        return csvToBean.parse(beanStrategy, reader);
    }

}
