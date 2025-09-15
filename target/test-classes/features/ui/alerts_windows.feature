@ui @alerts
Feature: Browser Windows
  Scenario: Nova janela e validação do texto
    Given que abro "https://demoqa.com/browser-windows"
    When eu clico no botão "New Window"
    Then devo ver uma nova janela com o texto "This is a sample page"
    And fecho a nova janela e volto para a janela original
