@ui @webtables
Feature: Web Tables CRUD
  Scenario: Criar, editar e deletar um registro
    Given que abro "https://demoqa.com/webtables"
    When eu crio um registro aleatório
    And eu edito o registro criado
    Then eu deleto o registro criado

  @bonus
  Scenario: Criar 12 registros e deletar todos
    Given que abro "https://demoqa.com/webtables"
    When eu crio "12" registros aleatórios
    Then eu deleto todos os registros criados
