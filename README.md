# MsgPreparer

MsgPreparer est une application android permettant :
- Afficher la liste des contacts
- Préparer/Supprimer des SMS à envoyer
- Spam/envoi rapide
- Envoi programmé
- Auto-réponse aux SMS
Testé physiquement sur un Xiaomi redmi note 9 et un Samsung S23

## Features techniques :
- Permissions demandés au démarrage, ainsi que la permission des alarmes/rappels pour les android >12, si refusée, fermeture de l'application 
- 4 tabs différentes
- Affichage liste des contacts (nom+numéro de téléphone)
- Case cochable pour chaque contact pour activer l'auto réponse 
- Ajout de messages personnalisés (160 bytes max)
- Affichage des messages personnalisés 
- Suppression des messages personnalisés
- Case cochable pour chaque message pour choisir un message spam (1 seule case)
- Case cochable pour chaque message pour choisir un message d'auto-reponse (1 seule case)
- Selection d'un contact dans la liste pour effectuer des actions
- Bouton d'envoi pour envoyer le message spam rapidement en SMS
- Activation de l'auto-réponse pour les contacts sélectionnés (à chaque réception de sms)
- Envoi programmé de messages
  
![Screenshot_2](https://github.com/teddyfresnes/LP_MsgPreparer/assets/80900011/88886c5d-ed1f-4d46-876b-06e080ee3d7f)  

## Structure du code
- MainActivity : activité principal de l'app
- activity_main.xml : layout pour ajouter la gestion des fragments
- MyPagerAdapter : gestion des changements entre fragments
- Fragment_Home : fragment home pour diriger l'user
- fragment_home.xml : layout pour l'accueil

- Fragment_Contacts : fragment qui importe les données contacts
- fragment_contacts.xml : layout affichage liste des contacts
- item_contact.xml : layout pour chaque item contact
- Contact : classe modèle
- ContactAdapter : gérer les changements des cases contacts

- Fragment_Messages : fragment qui importe les données messages
- fragment_messages.xml : layout affichage des messages
- item_message.xml : layout pour chaque item message (preview, sup, cases...)
- Message : classe modèle
- MessageAdapter : gérer les changements des messages/sauvegarde

- Fragment_Actions : gérer les actions, envoi de spam et appel des fonctions
- fragment_actions.xml : layout affichage des actions
- SMSService : service SMS en arrière plan
- SMSReceiver : classe à l'écoute des sms pour l'auto-réponse
- ScheduledMessageReceiver : classe reminder pour les messages programmés

## Bugs corrigés :
- Problèmes réalisation des fragments 
- Pour éviter les crash on demande les permissions à chaque ouverture de l'application, si refusée, on ferme avec un message 
- Contact dupliqués dans la liste des contacts réglé, tri alphabétique
- Sauvegarde des états des cases autoreply de chaque contact dans les sharedPreferences
- Sauvegarde des états des cases spam et autoreply de chaque message dans les sharedPreferences
- Suppression et ajout réactif/instantané des messages et sauvegarde dans les sharedPreferences
- Récupération des messages autoreply et spam dans la tab d'actions à partir de la position des checkbox dans les sharedPreferences 
- Message SMS non reçu sur un téléphone physique car les messages reçus sont des RCS
- Contact dupliqués réglé dans le spinner, tri alphabétique
- Pour l'auto-réponse, récupération difficile des contacts cochés dans les sharedPreferences
- Message auto-réponse ne s'envoi pas à cause du préfixe
