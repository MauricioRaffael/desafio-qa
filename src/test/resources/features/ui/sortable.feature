@ui @interactions
Feature: Sortable
  Scenario: Ordenar itens em ordem crescente usando drag and drop
    Given que abro "https://demoqa.com/sortable"
    When eu organizo a lista na ordem crescente
    Then a lista deve estar na ordem "One,Two,Three,Four,Five,Six"
