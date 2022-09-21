package io.github.rahulbsw.webhook.proxy;

public class Constants {
  public static String TEST_SPEC="" +
          "let idparts = split(.id, \"-\")\n" +
          "let xxx = [for ($idparts) \"x\" * size(.)]\n" +
          "\n" +
          "{\n" +
          "  \"id\" : join($xxx, \"-\"),\n" +
          "  \"type\" : \"Anonymized-View\",\n" +
          "  * : .\n" +
          "}\n";
}
