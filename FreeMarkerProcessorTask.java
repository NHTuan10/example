package com.example;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;


import java.io.*;
import java.util.*;

public class FreeMarkerProcessorTask extends DefaultTask {
    @TaskAction
    public void process(){

        List<String> dependencies =  new ArrayList<>();
        Project project = this.getProject();
        Configuration configuration = project.getConfigurations().getByName("compile");
        configuration.forEach(file -> {
            project.getLogger().lifecycle("Found project dependency @ " + file.getAbsolutePath());
//            System.out.println("Found project dependency @ " + file.getAbsolutePath());
            dependencies.add(file.getAbsolutePath());
        });

        // 1. Configure FreeMarker
        //
        // You should do this ONLY ONCE, when your application starts,
        // then reuse the same Configuration object elsewhere.

        freemarker.template.Configuration cfg = new freemarker.template.Configuration();

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(FreeMarkerProcessorTask.class,"");

        // Some other recommended settings:
//        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        try {
            Template template = cfg.getTemplate("template.ftl");
            // 2. Proccess template(s)
            //
            // You will do this for several times in typical applications.

            // 2.1. Prepare the template input:

            Map<String, Object> input = new HashMap<String, Object>();

            input.put("dependencies", dependencies);

            // 2.2. Get the template


            // 2.3. Generate the output

            // Write output to the console
            Writer consoleWriter = new OutputStreamWriter(System.out);
            template.process(input, consoleWriter);

            // For the sake of example, also write output into a file:
            Writer fileWriter = null;

            try {
                fileWriter = new FileWriter(new File("output.sh"));
                template.process(input, fileWriter);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TemplateException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }

    }
}
