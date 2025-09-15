@ui @forms
Feature: Practice Form
  Scenario: Preencher e submeter o formulário
    Given que abro "https://demoqa.com/login"
    When eu vou para "https://demoqa.com/automation-practice-form"
    And preencho o formulário com dados aleatórios e anexo "uploads/exemplo.txt"
    And submeto o formulário
    Then devo ver o popup de confirmação e fechá-lo
