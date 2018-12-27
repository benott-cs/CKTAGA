package edu.uri.cs.hypothesis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Ben on 12/26/18.
 */
public class HypothesisTest {

    @Test
    public void testHypothesisDump() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hypothesisToTest.json").getFile());
        Hypothesis h = mapper.readValue(file, Hypothesis.class);
        List<String> dump = h.getHypothesisDump();
    }

}