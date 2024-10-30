package org.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleTest {

    @Test
    void threePlusOneFour() {
       var example = new Example();

       int result = example.Add(3,1);

       assertEquals(4,result);
    }


    @Test
    void twoPlusTwoFour() {
        var example = new Example();

        int result = example.Add(2,2);

        assertEquals(4,result);
    }

    @Test
    void threePlusOneGivesFour(){
        var example = new Example();
        assertEquals(4,example.Add(3,1));
    }
}
