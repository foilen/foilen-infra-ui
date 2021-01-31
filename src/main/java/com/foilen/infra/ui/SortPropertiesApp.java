/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tuple.Tuple2;

public class SortPropertiesApp {

    public static void main(String[] args) throws Exception {

        sort("src/main/resources/WEB-INF/infra/ui/messages/messages_en.properties");
        sort("src/main/resources/WEB-INF/infra/ui/messages/messages_fr.properties");

    }

    private static void sort(String filename) throws Exception {

        System.out.println(filename);

        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(filename);
        properties.load(new InputStreamReader(inputStream, CharsetTools.UTF_8));
        CloseableTools.close(inputStream);

        PrintWriter printWriter = new PrintWriter(filename);
        properties.entrySet().stream() //
                .map(e -> new Tuple2<>((String) e.getKey(), (String) e.getValue())) //
                .sorted((a, b) -> a.getA().compareTo(b.getA())) //
                .forEach(e -> printWriter.println(e.getA() + "=" + e.getB()));
        CloseableTools.close(printWriter);

    }

}
