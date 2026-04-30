# Viešųjų darbų valdymo sistema

Trumpa instrukcija, kaip pasiruošti aplinką ir paleisti projektą lokaliai.

## Reikalavimai

Prieš paleisdami projektą įsitikinkite, kad turite įdiegtus šiuos įrankius:

* Java 21
* Maven 3.9 arba naujesnę versiją
* Node.js 20+ ir npm
* Docker
* PostgreSQL duomenų bazę

Pagal numatytą konfigūraciją (`handler/src/main/resources/application.yaml`):

* Host: `localhost`
* Portas: `5433`
* DB pavadinimas: `public_works_management`
* Vartotojas: `postgres`
* Slaptažodis: `admin`

Jei naudojate kitokius parametrus — juos reikės atnaujinti konfigūracijoje.

---

## Aplinkos konfigūracija

### 1. `.env` failo sukūrimas

Projekto šakniniame kataloge sukurkite `.env` failą ir pridėkite savo OpenAI API raktą bei MinIO prisijungimo duomenis:


```env
OPENAI_API_KEY=your_openai_api_key
MINIO_ROOT_USER=minio
MINIO_ROOT_PASSWORD=minioadmin
```

---

### 2. `application.yaml` patikrinimas

Failas:

```
handler/src/main/resources/application.yaml
```

Patikrinkite arba prireikus pakoreguokite šiuos parametrus:

* PostgreSQL:

  ```
  jdbc:postgresql://localhost:5433/public_works_management
  ```
* Qdrant (Spring AI):

  ```
  localhost:6334
  ```
* Qdrant HTTP adresas:

  ```
  http://localhost:6333
  ```
* MinIO:

  ```
  http://localhost:9100
  ```

Jeigu naudojate kitus portus ar prisijungimo duomenis — atitinkamai juos pakeiskite šiame faile.

---

## Projekto paleidimas

### 1. Infrastruktūros servisų paleidimas

Paleiskite Docker konteinerius:

```bash
docker compose up -d
```

Bus paleisti šie servisai:

* Qdrant

    * HTTP: `6333`
    * gRPC: `6334`
* MinIO

    * API: `9100`
    * Console: `9101`

---

### 2. Backend paleidimas (`customer` modulis)

```bash
./mvnw -pl customer spring-boot:run
```

---

### 3. Frontend paleidimas

```bash
cd client
npm install
npm start
```

---

## Viešųjų pirkimų duomenų importavimas

Duomenis į sistemą galima įkelti dviem būdais.

1 būdas naudoja jau paruoštą JSONL failą, kuris yra tinkamas tiesioginiam importavimui į Qdrant.
2 būdu galite patys atsisiųsti viešųjų pirkimų duomenis iš Open Contracting portalo transformuoti iškviečiant /api/import/transform API ir tada importuoti iškviečiant /api/import API.

### 1 būdas — paruošto failo importavimas į Qdrant

Naudokite vieną iš šių failų:

```
handler/src/main/java/org/handler/procurementData/prepared_for_embedding_procurement_data.jsonl
```



Tuomet iškvieskite API:

```bash
curl -X POST "http://localhost:8080/api/import?filePath=/absolute/path/prepared_for_embedding_procurement_data.jsonl"
```

---

### 2 būdas — transformavimas ir importavimas

Naudokite:

```
raw_procurement_data.jsonl
```

(arba atsisiųskite duomenis iš [Open Contracting](https://data.open-contracting.org/en/publication/68?) .jsonl formatu).

pirmiausia transformuokite:

```bash
curl -X POST "http://localhost:8080/api/import/transform?inputPath=/abs/path/raw_procurement_data.jsonl&outputPath=/abs/path/prepared_procurement_data.jsonl"
```

Tada importuokite:

```bash
curl -X POST "http://localhost:8080/api/import?filePath=/abs/path/prepared_procurement_data.jsonl"
```

---

## Duomenų importavimo API metodų paskirtis

### `POST /api/import/transform`

Transformuoja JSONL formato duomenis į sistemai tinkamą formatą:

* sugeneruojamas _embedding_ tekstas
* pridedami reikalingi metaduomenys

Rezultatas — paruoštas importavimui JSONL failas.

---

### `POST /api/import`

Nuskaito paruoštą JSONL failą ir įkelia duomenis į Qdrant vektorinę kolekciją:

```
contracts
```
