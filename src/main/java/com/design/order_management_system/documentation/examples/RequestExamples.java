package com.design.order_management_system.documentation.examples;

public class RequestExamples {
  private RequestExamples() {}

  public static final String LOGIN =
      """
            {
                "sUsername": "admin",
                "sPassword": "Admin@123"
            }
            """;
  public static final String USER_REGISTRATION =
      """
            {
                "sUsername": "JohnDoe",
                "sPassword": "Some$Password@432"
            }
            """;
  public static final String PRODUCT_REGISTRATION =
      """
            {
                "sProductName": "Sprite",
                "dPrice": 29.99,
                "dStock": 5
            }
            """;

  public static final String UPDATE_PRODUCT =
      """
            {
                "sNewProductName": "Diet Sprite",
                "dUpdatedPrice": 34.99,
                "dStockToAdd": 5
            }
            """;

  public static final String ORDER_REQUEST =
      """
            {
                "dProductId": 1,
                "dQuantity": 1
            }
            """;

  public static final String REGISTER_ORDER =
      """
            {
                "aOrderItems": [
                    {
                        "dProductId": 1,
                        "dQuantity": 1
                    },
                    {
                        "dProductId": 2,
                        "dQuantity": 1
                    }
                ]
            }
            """;
}
