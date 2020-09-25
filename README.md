# Bibliotheque
## Description
### À propos
**Système d'information pour bibliotheque.**  
Le projet respecte une **architecture orientée service**, il est composé des **microservices** (**batch**, **book-api**, **webui**) et des **edges microservices**(**config-server**, **eureka-server**, **zuul-server**). Tous ces microservices sont des projets **spring-boot** créés pour tourner de manière **autonome** sur des **serveurs tomcat**.  
Le projet est **sécurisé** par des appels vers [**keycloak**](https://www.keycloak.org/about.html "À propos de keycloak"), un **serveur de gestion d'identité et d'accès Opensource**.  
### Les microservices
#### Edges microservices
* ##### config-server
  **Serveur de configuration**, il est lié à un [**repository github**](https://github.com/Valaragen/bibliotheque-config), il contient des **informations de configuration** des microservices du projet. **Tous les microservices**, tentent à leurs **lancement**, de récupérer des informations via ce serveur de configuration.  
  **Port** : 9101  
* ##### eureka-server
  **'Registration server'** , toutes les **instances** des microservices du projet **s'inscrivent** sur ce server à leur lancement. Les microservices passent par ce **registre** pour appeler d'autres microservices.  
  **Port** : 9102  
* ##### zuul-server
  **API gateway**, le point d'entrée unique pour les API et microservices back-end(**book-api**)  
  **Port** : 9104
#### Microservices de la bibliotheque
* ##### book-api
  **API** permettant de **créer, lire, mettre à jour et/ou supprimer** des **livres**, **exemplaires** et **emprunts**.  
  **L'API contient** des **endpoints protégés**, reservés aux **utilisateurs authentifiés** ou aux **membres du staff**, pour **obtenir l'autorisation** d'appeler ces endpoints il faut **fournir un token d'accès valide** dans l'entête de la requête http.  
  Ce **token d'accès** est fourni par **keycloak** lors d'une **authentification réussie** via un **client autorisé** à lancer un **protocole d'authentification** (**ex: webui**)  
  **Base de données** : Postgresql  
  **Nom de la base de données** : MBOOK_DB  
  **Port** : 9001  
* ##### webui  
  **Site web pour les usagers de la bibliothèque**, permet de **consulter** la liste des livres disponibles à l'emprunt, **effectuer** une demande d'emprunt et de consulter ses emprunts.  
  **Port** : 9000  
* ##### batch
  **Service pour les traitements automatisés**, permet d’envoyer des **mails de relance** aux usagers n’ayant pas rendu les livres en fin de période de prêt. L’envoi est automatique à la **fréquence d’un par jour tous les jours à 11h30**.  
  **Frameworks** : Spring Batch, Spring Mail  
  **Base de données** : H2  
  **Port** : 9005  
##### Informations complémentaires
Les appels entre microservices sont gérés avec [OpenFeign](https://spring.io/projects/spring-cloud-openfeign) et son load balancés avec Ribbon, ces deux outils fonctionnent avec Eureka
#### Keycloak
Non integré sur ce repository git, keycloak est necessaire au fonctionnement des microservices de la bibliothèque. [Télécharger keycloak 8.0.1](https://www.keycloak.org/archive/downloads-8.0.1.html)  
**Version** : 8.0.1  
**Base de données** : Postgresql  
**Nom de la base de données** : KEYCLOAK_DB  
**Port** : 8080  
   
## Comment deployer le projet en local ?
### Prérequis
1. [Postgresql](https://www.postgresql.org/download/)
2. [Keycloak 8.0.1](https://www.keycloak.org/archive/downloads-8.0.1.html)
    1. [Configurer keycloak pour qu'il utilise une base de données potgresql](https://www.keycloak.org/docs/latest/server_installation/#_rdbms-setup-checklist) qui sera nommée **KEYCLOAK_DB**
### Installation
1. Télécharger la [**dernière release du projet**](https://github.com/Valaragen/bibliotheque/releases/).
2. Lancez pgAdmin et créez une nouvelle base de donnée nommée **MBOOK_DB** (Les tables et un jeu de données de base sera inséré automatiquement au premier lancement de la book-api)
3. Créez une base de données nommée **KEYCLOAK_DB** et insérez-y le script [**db_create_keycloak.sql**]().  
4. Lancez dans l'ordre :  
   1. **Keycloak** en mode standalone **/bin/standalone.bat(widows)** ou **/bin/standalone.sh(linux)**
   2. **config-server.jar**
   3. **eureka-server.jar**
   4. **zuul-server.jar**
   5. **book-api.jar**
   6. **webui.jar**  
   > Ne lancez pas _batch.jar_ pour le moment  
5. Vous pouvez désormais visiter l'application web à l'url **http://localhost:9000**  
   > Pour acceder à toutes les fonctionalités de l'application web vous pouvez vous **inscrire** ou vous **connecter** sur le compte utilisateur de test : **nom d'utilisateur:** user **mdp:** Azerty
6. Vous pouvez vous connecter à la console admin de keycloak via l'url **http://localhost:8080** avec les identifiants -> **nom d'utilisateur:** admin **mdp:** admin
7. Pour tester le module batch  
   1. Rendez vous à l'url **http://localhost:8080/auth/realms/bibliotheque/account/**
   2. Connectez vous à l'application web via **l'utilisateur test** :   
      **nom d'utilisateur:** user  
      **mot de passe:** Azerty  
   3. **Changez l'email** 'user@gmail.com' par l'email sur lequel vous souhaitez recevoir les mails de relance.
   4. Si ce n'est pas déjà fait, démarrez les services dans l'ordre indiqué à **l'étape 4**.
   5. Lancez batch.jar
      > Pour rendre les tests plus simple le module batch envoie les mails de relance à son lancement en plus de l'envoi automatique à 11h30 tous les jours.
8. Pour tester l'api web  
   1. Il vous faut **générer un token d'accès** depuis votre outil de test d'api à l'aide de **ces informations**:  
   **Token name:** keycloak-bearer-token  
   **Grant type:** Authorization code  
   **Callback URL:** http://localhost:9000/sso/login  
   **Auth URL:** http://localhost:8080/auth/realms/bibliotheque/protocol/openid-connect/auth  
   **Access token URL:** http://localhost:8080/auth/realms/bibliotheque/protocol/openid-connect/token  
   **Client ID:** webui  
   **Client secret:** 53ab1cbc-91e0-4699-a587-c4039eb48a12  
   **Scope:** openid profile email  
   **State:** 1234  
   **Client authentication:** Send client credential in body  
   >  Vous pouvez consulter les différents endpoints accessible via l'url http://localhost:9001/swagger-ui.html#/
9. Pour tester l'api web avec **des scénarios de tests d'integration postman**
   1. Depuis l'interface de keycloak, activez l'option **"direct grant"** pour le client **"webui"**
   2. **Téléchargez et importez** la [**collection postman** de l'api](https://github.com/Valaragen/P10-bibliotheque/tree/master/postman_API_integration_test).
   3. **Depuis postman**, lancez un des scénarios contenu dans le dossier **"integration test"**
## Diagrammes
### Base de donnée de l'api
![](https://github.com/Valaragen/P10-bibliotheque/blob/master/desc/class_diagram.JPG)  

## Aperçus
### Page de login
![](https://github.com/Valaragen/bibliotheque/blob/master/desc/connexion.JPG)  
### Page de consultation des livres
![](https://github.com/Valaragen/bibliotheque/blob/master/desc/livres.JPG)  
### Page de consultation des emprunts
![](https://github.com/Valaragen/bibliotheque/blob/master/desc/emprunts.JPG)  


