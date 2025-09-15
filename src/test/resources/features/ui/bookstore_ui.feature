@ui
Feature: Fluxo visual simples no Book Store
  Como usuário, quero fazer login no DemoQA Book Store e ver meu perfil.

  Scenario: Login básico e verificação do perfil
    Given que abro "https://demoqa.com/login"
    And eu preencho usuário e senha válidos
    And eu confirmo o login
    Then eu devo ver a página de perfil
