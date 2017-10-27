package com.backend.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.backend.domain.Profile;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;

public class CommonUtils {

    /**
     * 
     * @param clazz
     * @return
     * @throws JsonMappingException
     */
    public static <D> JsonSchema getSchema(final Class<D> clazz) throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SchemaFactoryWrapper schemaFactoryWrapper = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(clazz, schemaFactoryWrapper);
        return schemaFactoryWrapper.finalSchema();
    }

    /**
     * 
     * @param clazz
     * @return
     * @throws JsonMappingException
     */
    public static <D> Map<String, JsonSchema> getSchemaProperties(final Class<D> clazz) throws JsonMappingException {
        JsonSchema jsonSchema = getSchema(clazz);
        ObjectSchema object = jsonSchema.asObjectSchema();
        return object.getProperties();
    }

    public static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        }
        catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    public List<Profile> readWithCsvToBean(String filename) throws IOException {
        File file = new ClassPathResource(filename).getFile();
        FileInputStream fis = new FileInputStream(file);
        CSVReader reader = new CSVReader(new InputStreamReader(fis), '\t');
        ColumnPositionMappingStrategy<Profile> beanStrategy = new ColumnPositionMappingStrategy<Profile>();
        beanStrategy.setType(Profile.class);
        beanStrategy.setColumnMapping(new String[]{"ProfileId", "Cardholdername", "Email", "CardNumber", "Status", "Photo"});
        CsvToBean<Profile> csvToBean = new CsvToBean<Profile>();
        return csvToBean.parse(beanStrategy, reader);
    }

    /**
     * 
     * @param filename
     * @return
     * @throws IOException
     */
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
}
