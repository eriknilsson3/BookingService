# Booking Service - DevOps Teamarbete & Drift

## Projekt
Detta projekt är en del av kursuppgiften **DevOps - Teamarbete & Drift**.  
Gruppen arbetar som ett DevOps-team med fokus på samarbete, källkodsgranskning (code review), integration och kontinuerlig leverans (CI/CD), containerisering samt övervakning av tjänsten i drift.

**GitHub Repository:**  
https://github.com/eriknilsson3/BookingService

**Teammedlemmar:** Vanessa, Erik och Jennifer

---

## Länk till miljö
*   **Production:** https://bookingservice-production-badb.up.railway.app

Tjänsten driftsätts på molnplattformen Railway. Skillnader mellan lokal utvecklingsmiljö och produktionsmiljön hanteras genom miljövariabler.

---

## Branch-strategi

Vi använder **Trunk-Based Development**. All utveckling sker på separata feature-branches utifrån vår huvudgren. När en ändring är färdig och verifierad skapas en Pull Request mot `master`. Ingen i teamet pushar kod direkt till `master`.

Genom att kräva Pull Requests, code review och godkänd CI innan merge minskar vi risken för att felaktig eller otestad kod hamnar i huvudgrenen.

### Exempel på branch-namn:
```text
feature/add-actuator
feature/logging
feature/update-documentation
```

Vi valde Trunk-Based Development eftersom det passar vår teamstorlek och gör det möjligt att integrera förändringar ofta samtidigt som `master` hålls stabil.

---

## Development Workflow

Vårt arbetsflöde är uppbyggt enligt följande DevOps-kedja:
```text
Feature branch
      ▼
Pull Request
      ▼
Code Review
      ▼
CI / Tests
      ▼
Approval
      ▼
Merge till master
      ▼
Deployment till produktion
```
Ingen utveckling sker direkt på `master`. Alla ändringar går igenom en Pull Request och code review innan sammanslagning.

---

## Definition of Done

En ändring betraktas som klar när:
*   Ändringen är implementerad på en feature-branch.
*   Pull Request har skapats mot `master`.
*   CI-pipelinen rapporterar grön status.
*   Minst en annan gruppmedlem har granskat och godkänt ändringen.
*   Eventuella review-kommentarer har hanterats och åtgärdats.
*   Pull Requesten har mergats till `master`.

---

## Branch Protection

Vår huvudgren `master` är skyddad i GitHub-inställningarna med följande Branch Protection-regler:
*   **Require a pull request before merging:** Direkt push till `master` är blockerad för samtliga medlemmar.
*   **Require approvals:** Minst 1 godkännande (approval) från en annan gruppmedlem krävs innan merge tillåts.
*   **Require status checks to pass before merging:** Integrationskontrollen i GitHub Actions (CI) måste rapportera grön status innan koden får slås ihop.

Detta hjälper oss att förhindra att felaktig eller otestad kod hamnar direkt i huvudgrenen.

---

## Code Review - Granskningsprocess

Code review är en obligatorisk del av vårt arbetsflöde. Varje ändring granskas av någon annan i gruppen innan den mergas till `master`. Kommentarerna ska vara konkreta och konstruktiva för att utveckla koden.
*   **Vårt review-flöde:** Vanessa granskar Eriks PRs ➔ Erik granskar Jennifers PRs ➔ Jennifer granskar Vanessas PRs.

### Sammanställning av teamets bidrag:

| Medlem | PR skapade | PR granskade |
| :--- | :---: | :---: |
| **Vanessa** | *Fyll i vid inlämning* | *Fyll i vid inlämning* |
| **Erik** | *Fyll i vid inlämning* | *Fyll i vid inlämning* |
| **Jennifer** | *Fyll i vid inlämning* | *Fyll i vid inlämning* |

*De exakta länkarna till Pull Requests, reviews och feedback dokumenteras i varje medlems individuella beskrivning på Studentportalen när alla steg är slutförda.*

---

## Docker & CI/CD

Booking Service körs som en Docker-container. CI-pipelinen körs automatiskt i GitHub Actions **varje gång en Pull Request skapas eller uppdateras mot `master`**.

Pipelinen kontrollerar automatiskt att projektet kan byggas och att applikationens tester passerar. En Pull Request kan inte mergas om CI-statusen är röd.

Efter godkänd merge till `master` triggas vårt flöde för att paketera applikationen i en Docker-image och förbereda den för driftsättning på Railway.

---

## Logging

Applikationen använder logging för monitoring och felsökning i drift. Vi använder följande loggnivåer i källkoden:
*   **INFO** - Normal driftinformation (t.ex. inkommande förfrågningar, lyckade operationer).
*   **WARN** - Avvikelser som kan vara problematiska men som inte stoppar applikationen (t.ex. valideringsfel).
*   **ERROR** - Fel som kräver omedelbar felsökning och incidenthantering (t.ex. databasavbrott).

```java
// Kodexempel på loggning implementerad i källkoden (Vanessa)
log.info("Booking request received for customer {}", customerId); 
 
if (bookingData == null) { 
    log.warn("Booking request missing required fields"); 
} 
 
try { 
    bookingRepository.save(booking); 
} catch (Exception e) { 
    log.error("Failed to save booking to database", e); 
} 
```
*Säkerhetsprincip:* Lösenord, JWT-tokens, credentials och personuppgifter ska inte loggas.

---

## Health Check via Spring Boot Actuator

Vi använder **Spring Boot Actuator** för health monitoring.

Följande Actuator-endpoints exponeras och kan nås utan autentisering för att deployment-plattformen ska kunna kontrollera tjänstens status:
```text
/actuator/health
/actuator/info
```
*   **Production Health URL:** https://bookingservice-production-badb.up.railway.app/actuator/health

---

## Hantering av Merge-konflikt

En riktig merge-konflikt har uppstått och lösts inom gruppen under utvecklingens gång.
*   **Hur den uppstod:** Konflikten uppstod i filen `application.properties` när Jennifer och Erik ändrade på samma rad gällande miljökonfigurationen samtidigt i sina egna feature-branches. Erik hann merga sin PR till `master` först, vilket ledde till att Jennifers efterföljande Pull Request automatiskt blockerades av GitHub på grund av en konflikt.
*   **Hur den löstes:** Jennifer synkade sin lokala gren och drog ner de senaste ändringarna från huvudgrenen (`git pull origin master`). Git markerade källkodskonflikten med markörerna `<<<<<<< HEAD` och `>>>>>>>`. Jennifer och Erik utvärderade sina ändringar tillsammans och kom överens om vilken konfiguration som skulle behållas för produktionsmiljön. Jennifer tog därefter bort Gits konfliktmarkörer manuellt, sparade filen och committade den lösta ändringen innan PR-flödet slutfördes. Vanessa ansvarade efteråt för att dokumentera händelsen och lösningen i projektets README.

---

## Environment Variables & Projektstruktur

Miljöspecifika och känsliga värden hanteras via environment variables i Railway och hårdkodas inte direkt i repositoryt:
*   `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` - databasintegration
*   `JWT_SECRET` - autentisering
*   `CUSTOMER_SERVICE_BASE_URL` - extern mikrotjänst
*   `FRONTEND_ORIGIN` - CORS-inställningar

Projektet består av en Spring Boot-applikation förpackad som en Docker-container med REST API, Spring Security, JWT-autentisering, databasintegration och hälsomonitorering via Actuator.

---

## Sammanfattning

Vårt DevOps-arbetsflöde kombinerar utveckling, källkodsgranskning, CI/CD, Docker och deployment. Det övergripande flödet styrs enligt följande kedja:
```text
Feature branch ➔ Pull Request ➔ Code Review ➔ CI ➔ Skyddad master ➔ Production
```
Målet är att skapa ett strukturerat och säkert arbetsflöde där ändringar granskas, testas och kan driftsättas på ett kontrollerat sätt i vår driftmiljö.
