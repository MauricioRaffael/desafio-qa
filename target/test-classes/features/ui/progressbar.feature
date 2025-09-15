@ui @widgets
Feature: Progress Bar
  Scenario: Parar antes dos 25% e resetar aos 100%
    Given que abro "https://demoqa.com/progress-bar"
    When eu inicio a progress bar
    And paro antes de 25 por cento
    Then valido que a barra está em no máximo 25 por cento
    When inicio novamente até 100 por cento
    Then eu reseto a progress bar
