package itis.parsing;

import itis.parsing.annotations.FieldName;
import itis.parsing.annotations.MaxLength;
import itis.parsing.annotations.NotBlank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;


public class ParkParsingServiceImpl implements ParkParsingService {

    //Парсит файл в обьект класса "Park", либо бросает исключение с информацией об ошибках обработки
    @Override
    public Park parseParkData(String parkDatafilePath) throws ParkParsingException{
        return parseParkDataMap(parseParkDataIntoMap(parkDatafilePath));
    }

    private Map<String, String> parseParkDataIntoMap(String parkDatafilePath) throws ParkParsingException{
        List<String> fileStrings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(parkDatafilePath))) {
            String line = reader.readLine();
            if (!line.equals("***")){
                throw new IOException();
            }
            while (!(line = reader.readLine()).equals("***")){
                fileStrings.add(line);
            }
        } catch (IOException e) {
            throw new ParkParsingException("Wrong file format", null);
        }

        Map<String,String> parkDataMap = new HashMap<>();
        for (String string: fileStrings) {
            String[] splitString = string.split(":");
            String key = splitString[0].replace("\"", "").replace(" ", "");
            String value = splitString[1].replace("\"", "").replace(" ", "");
            parkDataMap.put(key, value);
        }
        return parkDataMap;
    }

    private Park parseParkDataMap(Map<String, String> parkDataMap) throws ParkParsingException {
        try {
            Class<Park> parkClass = Park.class;

            Constructor<Park> constructor = Park.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Park park = constructor.newInstance();

            Field legalNameField = parkClass.getDeclaredField("legalName");
            Field ownerOrganizationInnField = parkClass.getDeclaredField("ownerOrganizationInn");
            Field foundationYearField = parkClass.getDeclaredField("foundationYear");


            handleStringField(park, legalNameField, parkDataMap);
            handleStringField(park, ownerOrganizationInnField, parkDataMap);
            handleLocalDateField(park, foundationYearField, parkDataMap);

            return park;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void handleStringField(Park parkInstance, Field field, Map<String, String> map) throws ParkParsingException{
        String mapKey;
        int maxLength;
        boolean notBlank;

        FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);
        mapKey = !Objects.isNull(fieldNameAnnotation) ? fieldNameAnnotation.value() : field.getName();

        MaxLength maxLengthAnnotation = field.getAnnotation(MaxLength.class);
        maxLength = !Objects.isNull(maxLengthAnnotation) ? maxLengthAnnotation.value() : -1;

        NotBlank notBlankAnnotation = field.getAnnotation(NotBlank.class);
        notBlank = !Objects.isNull(notBlankAnnotation);

        String fieldValue = map.get(mapKey);

        List<ParkParsingException.ParkValidationError> parkValidationErrors = new ArrayList<>();

        if (fieldValue.length() > maxLength){
            parkValidationErrors.add(
                    new ParkParsingException.ParkValidationError(field.getName(),
                    "Ограничение размера стркои"));
        }
        if (fieldValue.equals("") && notBlank){
            parkValidationErrors.add(
                    new ParkParsingException.ParkValidationError(field.getName(),
                            "Поле не может быть пустым"));
        }

        if (!parkValidationErrors.isEmpty()){
            //throw new ParkParsingException("Значения полей не удовлетворяют ограничениям", parkValidationErrors);
        }

        try {
            field.setAccessible(true);
            field.set(parkInstance, fieldValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void handleLocalDateField(Park parkInstance, Field field, Map<String, String> map){
        String mapKey;
        int maxLength;
        boolean notBlank;

        FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);
        mapKey = !Objects.isNull(fieldNameAnnotation) ? fieldNameAnnotation.value() : field.getName();

        MaxLength maxLengthAnnotation = field.getAnnotation(MaxLength.class);
        maxLength = !Objects.isNull(maxLengthAnnotation) ? maxLengthAnnotation.value() : -1;

        NotBlank notBlankAnnotation = field.getAnnotation(NotBlank.class);
        notBlank = !Objects.isNull(notBlankAnnotation);

        String fieldValue = map.get(mapKey);

        List<ParkParsingException.ParkValidationError> parkValidationErrors = new ArrayList<>();
        if (fieldValue.length() > maxLength){
            parkValidationErrors.add(
                    new ParkParsingException.ParkValidationError(field.getName(),
                            "Ограничение размера стркои"));
        }
        if (fieldValue.equals("") && notBlank){
            parkValidationErrors.add(
                    new ParkParsingException.ParkValidationError(field.getName(),
                            "Поле не может быть пустым"));
        }

        if (!parkValidationErrors.isEmpty()){
            // throw new ParkParsingException("Значения полей не удовлетворяют ограничениям", parkValidationErrors);
        }

        String[] splitFieldValue =  fieldValue.split("-");
        int year = Integer.parseInt(splitFieldValue[0]);
        int month = Integer.parseInt(splitFieldValue[1]);
        int dayOfMonth = Integer.parseInt(splitFieldValue[2]);
        LocalDate localDate = LocalDate.of(year, month, dayOfMonth);

        try {
            field.setAccessible(true);
            field.set(parkInstance, localDate);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

