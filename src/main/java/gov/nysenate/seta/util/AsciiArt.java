package gov.nysenate.seta.util;

public enum AsciiArt
{
    TS_LOGO("\n\n" +
        "███████╗███████╗███████╗              ███████╗███████╗████████╗ █████╗ \n" +
        "██╔════╝██╔════╝██╔════╝              ██╔════╝██╔════╝╚══██╔══╝██╔══██╗\n" +
        "█████╗  ███████╗███████╗    █████╗    ███████╗█████╗     ██║   ███████║\n" +
        "██╔══╝  ╚════██║╚════██║    ╚════╝    ╚════██║██╔══╝     ██║   ██╔══██║\n" +
        "███████╗███████║███████║              ███████║███████╗   ██║   ██║  ██║\n" +
        "╚══════╝╚══════╝╚══════╝              ╚══════╝╚══════╝   ╚═╝   ╚═╝  ╚═╝\n" +
        "Deploying on DATE\n");

    String text;

    AsciiArt(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
