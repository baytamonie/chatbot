package com.just.chatbot.controllers;


import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.CompletionService;

@Controller
@RequestMapping("/chat")
public class Chat {

    private String engine;
    private final OpenAiService service;
    private List<String> list;
    private String precautionOne;
    private String precautionTwo;
    private String precautionThree;
    private String precautionFour;
    private String description;

    @Autowired
    public Chat(@Value("${serviceKey}") String key,@Value("${serviceEngine}") String engine){
        service = new OpenAiService(key);
        this.engine = engine;
        list = new LinkedList<>();
        list.add("END");
    }

    @GetMapping
    public String chatPost(){
        return "chatbot";
    }
    @PostMapping
    public String returnAnswer(@RequestParam("prompt") String prompt){
        prompt = prompt +"->";
        String completion = getCompletion(prompt);
        List<String> info = getInfo(completion);


        return "output";

    }

    public String getCompletion (String prompt){
        String completion = " ";
        do{
        try{
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(prompt)
                .stop(list)
                .echo(true)
                .build();
         completion = service.createCompletion(engine, completionRequest)
                .getChoices()
                .get(0)
                .getText()
                .split("->")[1];

         if (completion.charAt(0)==' ')
             completion = completion.substring(1,completion.length()-1);
         break;
        }

        catch (Exception e){

        }
            } while (true);
        return completion;
    }
    public List<String> getInfo (String completion){
        List<String> list = new LinkedList<>();
        try
        {
            File file = new File("src/main/java/com/just/chatbot/symptoms.xlsx");
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);
            for(Row row : sheet){
                String cellZero = row.getCell(0).toString();
                if(cellZero.toLowerCase().equals(completion)){
                    description = row.getCell(5).toString();
                    precautionOne = row.getCell(1).toString();
                    precautionTwo = row.getCell(2).toString();
                    precautionThree = row.getCell(3).toString();
                    precautionFour = row.getCell(4).toString();
                    list.add(description);
                    list.add(precautionOne);
                    list.add(precautionTwo);
                    list.add(precautionThree);
                    list.add(precautionFour);
                    break;
                }
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return  list;
    }





}
