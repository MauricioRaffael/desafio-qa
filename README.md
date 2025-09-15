📘 Projeto Desafio QA

📋 Sobre o Projeto

Este repositório contém a automação de testes end-to-end para o site DemoQA, desenvolvida como parte do Desafio de QA.

O objetivo do desafio foi:

Implementar cenários de teste em BDD utilizando Cucumber.

Automatizar interações com formulários, tabelas, janelas e widgets.

Garantir execução estável em modo headless (sem interface gráfica) e modo normal (com interface).

🚀 Tecnologias Utilizadas

Java 17

Maven 3.9+

Selenium WebDriver 4.22+

Cucumber JVM 7+

JUnit 5

AssertJ para validações

Chromedriver (compatível com a versão do Chrome instalada)

⚙️ Pré-requisitos

Antes de rodar os testes, garanta que possui:

JDK 17+
instalado

Maven 3.9+
instalado

Google Chrome
atualizado

ChromeDriver
compatível com a versão do seu Chrome

▶️ Como Executar os Testes
1. Clonar
git clone https://github.com/MauricioRaffael/desafio-qa.git

cd desafio-qa

3. Rodar apenas API
mvn test "-Dcucumber.filter.tags=@api"

4. Rodar segundo desafio (exemplos)
mvn test "-Dcucumber.filter.tags=@forms" "-Dheadless=true"

mvn test "-Dcucumber.filter.tags=@windows" "-Dheadless=true"

mvn test "-Dcucumber.filter.tags=@tables" "-Dheadless=true"

mvn test "-Dcucumber.filter.tags=@progress" "-Dheadless=true"

mvn test "-Dcucumber.filter.tags=@sortable" "-Dheadless=true"

5. Rodar tudo visível (debug)

mvn test "-Dheadless=false"

📊 Relatórios

Após a execução, os relatórios podem ser consultados em:

target/surefire-reports/
