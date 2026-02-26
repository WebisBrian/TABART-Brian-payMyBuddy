# Pay My Buddy – Guide de démarrage


## Description

Pay My Buddy est une application web Java permettant aux utilisateurs de transférer de l’argent simplement à leurs contacts.

Le projet est développé avec **Spring Boot**, selon une approche **DB-first** : la base de données est la source de vérité et les entités JPA doivent correspondre au schéma SQL existant.

Ce document explique **pas à pas** comment démarrer le projet **Pay My Buddy** et intégrer les données nécessaires à son fonctionnement.

---

## Stack technique

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA (Hibernate)
* Thymeleaf
* MySQL
* Maven

**Assurez-vous également d'avoir** :
* MySQL installé et démarré
* Un client SQL (MySQL Workbench, DBeaver, DataGrip, phpMyAdmin, etc.)

---

## 1. Initialisation de la base de données (obligatoire)

La base de données s'appuie sur le **Diagramme de Classes** et le **Modèle Physique de Données** suivants :
* [Voir le Diagramme de Classe](/.readme/class_diagram.png)
* [Voir le Modèle Physique de Données](/.readme/implementation_diagram.png)

Le fichier `schema.sql` est **indispensable** au fonctionnement de l’application. Il crée :

* la base de données `pay_my_buddy`
* les tables
* les contraintes (clés étrangères, unicité)

### Étapes

1. Ouvrir votre client MySQL
2. Ouvrir le fichier :

   ```
   sql/schema.sql
   ```
3. Exécuter le script **en totalité**

> À ce stade, la base de données et sa structure sont prêtes.

---

## 2. Chargement des données de démonstration (optionnel)

Un jeu de données est fourni pour faciliter l’évaluation et les tests fonctionnels.

### Étapes

1. Ouvrir le fichier :

   ```
   sql/data.sql
   ```
2. Vérifier que la base `pay_my_buddy` est sélectionnée
3. Exécuter le script **en totalité**

Les données peuvent être réexécutées sans risque (le script nettoie les tables avant insertion).

### Comptes de démonstration

* Emails :

    * `alice@paymybuddy.local`
    * `bob@paymybuddy.local`
    * `chloe@paymybuddy.local`
    * `david@paymybuddy.local`
  
* Mot de passe (tous les comptes) :

  ```
  password
  ```

---

## 3. Configuration de l’application

Le projet utilise une configuration externalisée, afin d’éviter toute donnée sensible codée en dur.

Un fichier d’exemple est fourni :
```
application-dev.properties.example
```

### Configuration locale (recommandée)

Pour lancer le projet en local :
1. Copier le fichier d'exemple
2. Le renommer en ```application-dev.properties```
3. Adapter les valeurs si nécessaire
4. L'application devra être lancée avec le profil `dev`


### Valeurs par défaut

* utilisateur : `root`
* mot de passe : `root`
* base : `pay_my_buddy`

---

## 4. Lancement de l’application

### ⚠️ Lancement avec le profil `dev`

L'idéal est de lancer l'application à l'aide d'une variable d’environnement :

#### Linux / macOS

```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

#### Windows (PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
./mvnw spring-boot:run
```

L’application démarre sur :

```
http://localhost:8080
```
---

## 5. Lancement des tests

Vous pouvez lancer les tests en utilisant la commande suivante :

```bash
mvn verify
```

---

## Notes techniques importantes

* Le projet est **DB-first** : Hibernate ne crée ni ne modifie les tables
* `spring.jpa.hibernate.ddl-auto=validate` est utilisé pour vérifier la cohérence entités / schéma
* Les scripts SQL ne sont pas exécutés automatiquement au démarrage
* Cette approche évite toute réinitialisation involontaire des données

---
