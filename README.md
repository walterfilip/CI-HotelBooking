# HotelBooking

HotelBooking är ett bokningssystem för ett pensionat. Systemet är uppdelat i två Spring Boot-tjänster som har var sin MySQL-databas.

## Tjänster

### HotelBooking

HotelBooking körs på port `8080` och ansvarar för:

- webbgränssnittet med Thymeleaf
- rum och sökning efter lediga rum
- att skapa, ändra och avboka bokningar
- att kontrollera att rum inte dubbelbokas

En bokning sparar kundens id, men inte kundens övriga uppgifter.

### CustomerService

CustomerService körs på port `8081` och ansvarar för:

- registrering av kunder
- inloggning
- hämtning och uppdatering av kunduppgifter
- borttagning av kunder
- hashning och kontroll av lösenord

## Kommunikation mellan tjänsterna

Tjänsterna kommunicerar med REST-anrop och läser aldrig direkt från varandras databaser.

HotelBooking anropar CustomerService för att registrera och logga in kunder samt för att hämta, ändra och ta bort kunduppgifter. När en bokning skapas kontrollerar HotelBooking att kunden finns.

CustomerService finns i en separat projektmapp bredvid HotelBooking:

```text
IdeaProjects/
├── HotelBooking/
└── CustomerService/
```

Den här placeringen behövs eftersom `compose.yaml` bygger båda projekten.

## Starta med Docker

Du behöver Docker Desktop.

1. Kontrollera att mapparna `HotelBooking` och `CustomerService` ligger bredvid varandra.
2. Gå till mappen `HotelBooking`.
3. Skapa `.env` från exemplet och välj ett lösenord för MySQL:

```powershell
Copy-Item .env.example .env
```

4. Starta hela systemet:

```bash
docker compose up --build
```

Webbplatsen finns sedan på [http://localhost:8080](http://localhost:8080).

Docker Compose startar:

- HotelBooking
- CustomerService
- en MySQL-databas för bokningar och rum
- en MySQL-databas för kunder

Stoppa systemet med:

```bash
docker compose down
```

Lägg till `-v` om även databasernas Docker-volymer ska tas bort:

```bash
docker compose down -v
```

## Starta lokalt utan Docker

Skapa MySQL-databaserna `hotelbooking` och `customerservice`. Starta sedan CustomerService först och HotelBooking därefter.

I respektive projektmapp:

```powershell
.\mvnw.cmd spring-boot:run
```

Standardinställningarna är:

- HotelBooking: `http://localhost:8080`
- CustomerService: `http://localhost:8081`
- MySQL-användare: `root`
- MySQL-lösenord: `secret`

Inställningarna kan ändras med miljövariablerna `PORT`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` och `CUSTOMER_SERVICE_BASE_URL`.

## REST API

HotelBooking:

- `GET /api/rooms` – hämta alla rum
- `GET /api/bookings` – hämta alla bokningar
- `POST /api/bookings` – skapa en bokning
- `PATCH /api/bookings/{id}/cancel` – avboka en bokning
- `PATCH /api/bookings/{id}/changedate` – ändra bokningsdatum

CustomerService:

- `POST /api/customers` – skapa en kund
- `GET /api/customers/{id}` – hämta en kund
- `POST /api/customers/login` – logga in
- `PUT /api/customers/{id}` – uppdatera en kund
- `DELETE /api/customers/{id}` – ta bort en kund

## Tester

Tester körs i respektive projekt med:

```powershell
.\mvnw.cmd test
```

## Teknik

- Java 25
- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven
- Docker och Docker Compose

## Teamarbete och DevOps-flöde

Vi har jobbat trunk-based med en skyddad huvudbranch `master` och kortlivade feature-branches. Det passade vårt lilla team eftersom vi kunde göra mindre ändringar och slå ihop dem ofta.

Vårt arbetsflöde:

1. Vi skapar en feature-branch från `master`.
2. När ändringen är klar öppnar vi en pull request.
3. CI bygger projektet och kör testerna automatiskt.
4. En annan gruppmedlem granskar koden och lämnar feedback.
5. När CI är grön och PR:en är godkänd kan den mergas.
6. Efter merge deployas den nya versionen till Railway.

Vi pushar inte direkt till huvudbranchen. Branch protection kräver en godkänd review och grön CI innan merge.

## Merge-konflikten

Vi ville testa att hantera en merge-konflikt och gjorde därför loggningen på två olika feature-branches. Att loggningen hamnade i olika klasser var däremot en slump, på den ena branchen låg den i `BookingController` och på den andra i `BookingService`. Det ledde till en riktig diskussion om vilken placering som var bäst. Den första PR:en behövde sedan ändras lite för att ändringarna skulle krocka i samma kod [pull request #18](https://github.com/walterfilip/CI-HotelBooking/pull/18).

Båda brancherna fick varsin PR och när den andra skulle mergas fick vi konflikten. Vi valde inte bara en av versionerna, utan tog delar från båda. Den ena hade bättre variabelnamn medan den andra hade lite renare kod. Loggningen hamnade till slut i `BookingService`, eftersom det är där logiken för bokningarna finns, medan felloggningen fick ligga kvar i `GlobalExceptionHandler`. Konflikten var alltså planerad, men vi löste den på samma sätt som en naturlig konflikt: vi jämförde lösningarna, diskuterade vad som var bäst och kombinerade koden. Efteråt kontrollerade vi att projektet byggde och att testerna gick igenom. Den färdiga ändringen finns i [pull request #19](https://github.com/walterfilip/CI-HotelBooking/pull/19).

## Loggning och observerbarhet

Vi använder olika loggnivåer beroende på vad som händer:

- `INFO` när en bokning skapas, avbokas eller ändras.
- `WARN` vid till exempel misslyckad inloggning eller nekad åtkomst.
- `ERROR` när ett tekniskt fel uppstår, till exempel om CustomerService inte går att nå.

Vi loggar aldrig lösenord, tokens eller personuppgifter.

Spring Boot Actuator ger oss [`/actuator/health`](https://ci-hotelbooking-production.up.railway.app/actuator/health), som visar om tjänsten är igång. Railway använder den som health check. Vi visar inte några detaljer utåt eftersom de kan innehålla intern information. Actuator lades till i [pull request #16](https://github.com/walterfilip/CI-HotelBooking/pull/16).

## Driftsatt tjänst

Tjänsten är deployad på [Railway](https://ci-hotelbooking-production.up.railway.app/).
