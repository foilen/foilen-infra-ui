/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import org.kohsuke.args4j.Option;

/**
 * The arguments to pass to the infra ui web application.
 */
public class InfraUiOptions {

    @Option(name = "--debug", usage = "To log everything (default: false)")
    public boolean debug;

    @Option(name = "--configFile", usage = "The config file path (default: none since using the CONFIG_FILE environment variable)")
    public String configFile;

    @Option(name = "--mode", usage = "The mode: TEST, PROD (default: PROD)")
    public String mode = "PROD";

}
