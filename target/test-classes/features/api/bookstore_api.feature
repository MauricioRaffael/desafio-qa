@api
Feature: Fluxo de API no DemoQA (BookStore)
  Para garantir que a API funciona de ponta a ponta,
  eu quero criar um usuário, autorizar, listar livros, alugar 2 e conferir no perfil.

  Background:
    Given que a baseUrl da API é "https://demoqa.com"

  Scenario: Criar usuário, autorizar e alugar 2 livros
    When eu crio um usuário aleatório válido
    And eu gero um token para esse usuário
    And eu verifico que o usuário está autorizado
    And eu listo os livros disponíveis
    And eu adiciono 2 livros ao usuário
    Then eu vejo o usuário com exatamente 2 livros
