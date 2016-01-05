package com.awplab.core.test;

import com.awplab.core.rest.jackson.JacksonManagerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

/**
 * Created by andyphillips404 on 12/17/15.
 */

@Command(scope = "test", name = "test-jackson")
@Service
public class TestJacksonCommand implements Action {


    @Reference
    JacksonManagerService jacksonManagerService;


    @Override
    public Object execute() throws Exception {

        ShellTable table = new ShellTable();
        table.column("Name");
        table.column("Class");


        ObjectMapper objectMapper  = new ObjectMapper();
        jacksonManagerService.registerModulesWithObjectMapper(objectMapper);

        table.print(System.out);

        return null;
    }
}
