package uk.co.deloitte.banking.ahb.dtp.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Slf4j
public class AlphaTestUsers {
    public List<AlphaTestUser> alphaTestUsers = new ArrayList<>();

    public static final String USERS_FILE = "./users.json";

    public void writeToFile() {
        log.info("Dumping {} alphaTestUsers to file", alphaTestUsers.size());
        ObjectMapper objectMapper = new ObjectMapper();

        try (FileWriter fileWriter = new FileWriter(USERS_FILE)) {
            objectMapper.writeValue(fileWriter, this);
        } catch (IOException ex) {
            log.error("Unable to write alphaTestUsers to file", ex);
        }
    }
}
