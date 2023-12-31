package data_access;

import entity.Medicine;
import entity.MedicineFactory;
import entity.Today;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class MedicineDAO implements MedicineDataAccessInterface{
    /**
     * A data access object for manipulating data stored in entities, as well as handling processing of JSONs for data
     * persistence.
     */
    private final File jsonFile;
    private HashMap<String, Medicine> userMedicines = new HashMap<>();
    private Today today;
    private final MedicineFactory medicineFactory;

    /**
     * Constructs a MedicineDAO data access object.
     * @param jsonPath, the file path from which to access the data persistence layer (JSON file).
     * @param today1, an entity representing the current day.
     * @param medicineFactory, a factory to create Medicine and Dose entities for storing data in memory.
     * @throws IOException, the exception thrown if the DAO cannot read the file.
     */
    public MedicineDAO(String jsonPath, Today today1, MedicineFactory medicineFactory) throws IOException {
        this.today = today1;
        this.medicineFactory = medicineFactory;
        this.jsonFile = new File(jsonPath);

        if (jsonFile.length() == 0) {
            save();
        } else {
            try {
                String content = new String(Files.readAllBytes(Paths.get(jsonFile.toURI())));
                JSONObject file = new JSONObject(content);
                JSONObject today = (JSONObject) file.get("today");
                JSONArray todayArray = (JSONArray) today.get("todayArray") ;
                JSONArray medArray = (JSONArray) file.get("medicines");
                for (Object object1:medArray) {
                    JSONObject med = (JSONObject) object1;
                    Integer dS = (Integer) med.get("doseSize");
                    Integer dI = (Integer) med.get("doseInventory");
                    Integer su = (Integer) med.get("sun");
                    Integer mo = (Integer) med.get("mon");
                    Integer tu = (Integer) med.get("tue");
                    Integer we = (Integer) med.get("wed");
                    Integer th = (Integer) med.get("thu");
                    Integer fr = (Integer) med.get("fri");
                    Integer sa = (Integer) med.get("sat");
                    Integer[] weeklySchedule = {su, mo, tu, we, th, fr, sa};
                    Medicine medicine = medicineFactory.createMedicine((String) med.get("name"), dS, dI,
                            (String) med.get("doseUnit"), weeklySchedule, (String) med.get("description"), (String) med.get("id"));
                    userMedicines.put(medicine.getName(), medicine);
                } if (today.get("dayInt") == this.today.getDay()) {
                    for (Object object1:todayArray) {
                        JSONObject med = (JSONObject) object1;
                        Integer t = (Integer) med.get("taken");
                        this.today.add((String) med.get("name"), t);
                    }
                } else {
                    for (Medicine medicine:userMedicines.values()) {
                        if (medicine.getWeeklySchedule()[this.today.getDay()] != 0) {
                            this.today.add(medicine.getName(), 0);
                        }
                    }
                }
            } catch (IOException e) {
                throw new IOException();
            }
        }
    }
    private void save() {
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            JSONObject file = new JSONObject();
            JSONObject today = new JSONObject();
            today.put("dayInt", this.today.getDay());
            JSONArray dayArray = new JSONArray();
            HashMap<String, Integer> todayChecklist = this.today.getTodayChecklist();
            for (String medicine:todayChecklist.keySet()) {
                JSONObject med = new JSONObject();
                med.put("name", medicine);
                med.put("taken", todayChecklist.get(medicine));
                dayArray.put(med);
            } today.put("todayArray", dayArray);

            JSONArray medArray = new JSONArray();
            for (Medicine medicine:userMedicines.values()) {
                JSONObject med = new JSONObject();
                med.put("name", medicine.getName());
                med.put("doseSize", medicine.getDose().getSize());
                med.put("doseInventory", medicine.getDose().getInventory());
                med.put("doseUnit", medicine.getDose().getUnit());
                med.put("sun", medicine.getWeeklySchedule()[0]);
                med.put("mon", medicine.getWeeklySchedule()[1]);
                med.put("tue", medicine.getWeeklySchedule()[2]);
                med.put("wed", medicine.getWeeklySchedule()[3]);
                med.put("thu", medicine.getWeeklySchedule()[4]);
                med.put("fri", medicine.getWeeklySchedule()[5]);
                med.put("sat", medicine.getWeeklySchedule()[6]);
                med.put("description", medicine.getDescription());
                med.put("id", medicine.getId());
                medArray.put(med);
            }
            file.put("today", today);
            file.put("medicines", medArray);
            fileWriter.write(file.toString());
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Checks if a specific medicine exists in the DAO.
     * @param name, the name of the medicine.
     * @return true if the DAO contains the medicine, false otherwise.
     */
    public boolean exists(String name){
        return userMedicines.containsKey(name);
    }

    /**
     * Saves the medicine in the DAO and the file.
     * @param medicine, the Medicine entity to be stored.
     */
    public void saveMedicine(Medicine medicine){
        userMedicines.put(medicine.getName(), medicine);
        if (medicine.getWeeklySchedule()[today.getDay()] != 0) {
            today.add(medicine.getName(), 0);
        }
        save();
    }

    /**
     * Deletes the medicine in the DAO and the file.
     * @param medicine, the name of the medicine to be deleted.
     */
    public void  removeMedicine(String medicine){
        userMedicines.remove(medicine);
        today.remove(medicine);
        save();
    }

    /**
     * Takes one dose of the medicine and updates the entity and file accordingly.
     * @param medicine, the name of the medicine taken.
     */
    public void takeMedicine(String medicine) {
        userMedicines.get(medicine).getDose().takeDose();
        today.take(medicine);
        save();
    }

    /**
     * Reverts the taking of one dose of the medicine and updates the entity and file accordingly.
     * @param medicine, the name of the medicine taken.
     */
    public void undoTakeMedicine(String medicine){
        userMedicines.get(medicine).getDose().undoTakeDose();
        today.untake(medicine);
        save();
    }

    /**
     * Gets all medicine IDs for API processing.
     * @return a string of all medicine IDs formatted for an API endpoint.
     */
    @Override
    public String getIdListString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (userMedicines.values().isEmpty()) {return "";}
        else {
            for (Medicine medicine:userMedicines.values()) {
            stringBuilder.append(medicine.getId()).append("+");
            } stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return stringBuilder.toString();
        }
    }

    /**
     * Gets the current day.
     * @return an integer representing the current day.
     */
    public Integer getTodayDay() {return today.getDay();}

    /**
     * Gets the checklist from the Today entity.
     * @return a hashmap containing information pertaining to the number of doses taken per medicine on the current day.
     */
    public HashMap<String, Integer> getTodayChecklist() {return today.getTodayChecklist();}

    /**
     * Gets the medicines stored in the DAO.
     * @return a hashmap containing all medicines stored in the DAO.
     */
    public HashMap<String, Medicine> getUserMedicines() {return userMedicines;}

    /**
     * Gets a DAO for the current day.
     * @param localDate, the current day.
     * @param jsonPath, the path of the file to read from/write to.
     * @return a DAO for the current day that reads and writes to the specified file.
     */
    public static MedicineDAO getMedicineDAO(LocalDate localDate, String jsonPath) {
        String day = localDate.getDayOfWeek().name();
        Integer dayInt = null;
        switch (day) {
            case "SUNDAY" -> dayInt = 0;
            case "MONDAY" -> dayInt = 1;
            case "TUESDAY" -> dayInt = 2;
            case "WEDNESDAY" -> dayInt = 3;
            case "THURSDAY" -> dayInt = 4;
            case "FRIDAY" -> dayInt = 5;
            case "SATURDAY" -> dayInt = 6;
        }
        try {
            return new MedicineDAO(jsonPath, new Today(dayInt), new MedicineFactory());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
