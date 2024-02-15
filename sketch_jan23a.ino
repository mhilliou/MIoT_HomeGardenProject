#include <WiFi.h>
#include <PubSubClient.h>
#include <Wire.h>
#include <NewPing.h>
#include <DHT.h>
#include <ArduinoJson.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <Preferences.h>

/////////////ACCES AUX WIFI//////////////////////
#define SSID "SSID_name"
#define PASSWORD  "password"

/////////////////INFORMATIONS MQTT////////////////
#define MQTT_PORT 1883
#define MQTT_SERVER  "x.x.x.x"   //adresse du borker mqtt

//////////////IDENTIFIANT UNIQUE POUR L'ESP32///////////
#define CLIENT_ID "ESP32ClientUnique"

/////////LES TOPIC POUR LA SOUSCRIPTION////////
#define TOPIC_POMPE "receive/pump"
#define TOPIC_LED "receive/led"
#define TOPIC_LUMINOSITE "send/luminosity"
#define TOPIC_BUTTON_POUSSOIR "send/button"
#define TOPIC_TEMPERATURE "send/temperature"
#define TOPIC_HUMIDIDTE "send/humidity"
#define TOPIC_LEVEL "send/waterLevel"
#define TOPIC_SCHEDULE_PUMP "receive/scheduledPump"

////////Brochage commande manuelle
#define pump 13 ///broche relais 
#define inputPin 2 //broche bouton poussoir 

///// DEFINITION DES BROCHES POUR LE CAPTEUR ULTRASON
#define ECHO_PIN 15 
#define DISTANCE_MAX 30+
NewPing sonar(ECHO_PIN, ECHO_PIN, DISTANCE_MAX);
int waterLevel;

/////DEFINITION BROCHAGE DHT11
#define DHTPIN 17 //broche dht11
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

/////DEFINITION DES BROCHES LUMINOSITE
#define sensorPin 36

/////CONFIGURATION ESP/WIFI
WiFiClient espClient;
PubSubClient client(espClient);
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 3600, 60000); // ajuste le décalage horaire selon la localisation
Preferences preferences;

///Connexion au wifi
void setup_wifi() {
  //Recherche de connexion
  Serial.print("Tentative de connexion à ");
  Serial.println(SSID);
  ///Connexion
  WiFi.begin(SSID, PASSWORD);
  //Nombre de tentative de connexion
  int attempts = 0;

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connexion au WIFI...");
    attempts++;
    if (attempts > 30) {
      Serial.println("Failed to connect to WiFi. Restarting ESP32.");
      ESP.restart();
    }
  }
}

//Connexion au broker mqtt
void setup_mqtt() {
  Serial.println("Connecting to MQTT broker...");
  // Configuration pour la connexion au client MQTT
  client.setServer(MQTT_SERVER, MQTT_PORT);
  client.setCallback(callback);
  client.setClient(espClient);

  /// connexion réussie ou pas 
  while (!client.connected()) {
    Serial.println("Reconnecting to MQTT broker");
    if (client.connect(CLIENT_ID)) {
      Serial.println("Connected to MQTT broker");
      //Abonnement au topic pompe 
      client.subscribe(TOPIC_POMPE);
      //Abonnement au calendrier pompe 
      client.subscribe(TOPIC_SCHEDULE_PUMP);
    } else {
      Serial.println("Failed to connect to MQTT broker");
      delay(5000);
    } 
}
}

void setup() {
  Serial.begin(115200);
  // Appel de la fonction de configuration WiFi
  setup_wifi();
  // Appel de la fonction de configuration MQTT
  setup_mqtt();

  ///Configuration de la commande manuelle de la pompe 
  pinMode(pump, OUTPUT);      // declare LED as output
  pinMode(inputPin, INPUT);     // declare pushbutton as input
 
  ////Configuration DHT11
  dht.begin();
  timeClient.begin();
  preferences.begin("schedule", false);

}


void callback(char* topic, byte* payload, unsigned int length) {
  // Convertir le payload en string pour faciliter la comparaison
  String messageTemp;
  for (int i = 0; i < length; i++) {
    messageTemp += (char)payload[i];
  }
  //Reception des commandes du topic de la pompe pour l'activer à distance
  if (String(topic) == TOPIC_POMPE) {
    Serial.println(messageTemp);
    StaticJsonDocument<200> jsonDoc;
    DeserializationError error = deserializeJson(jsonDoc, messageTemp);
  ///// en cas derreur de traitement
  if (error) {
    Serial.print("Erreur lors de la désérialisation du JSON: ");
    Serial.println(error.c_str());
    return;
  }
  // Extraire la valeur associée à la clé "active_pump"
    const char* activePumpValue = jsonDoc["active_pump"];
    // Vérifier si la valeur est "on"
    if (strcmp(activePumpValue, "on") == 0) {
      digitalWrite(pump, HIGH); // Allume la pompe
      delay(3000);
      Serial.println("Pompe allumée");
    }

  }
  ///Si on recoit les commandes du topic du calendrier de la pompe pour l'activer au jour et à l'heure indiquée
  if (String(topic) == TOPIC_SCHEDULE_PUMP) {
  Serial.print(messageTemp);
  StaticJsonDocument<512> doc;
  deserializeJson(doc, messageTemp);
  JsonObject obj = doc["scheduledPump"];
  for (int i = 0; i < 7; i++) {
    String day = obj[dayOfWeek(i)];
    if (!day.equals("")) {
      const char* dayOfWeekChar = dayOfWeek(i).c_str();
      preferences.putString(dayOfWeekChar, day.c_str());
  }
}
}
}

void loop() {
  
  //Reception des messages
  client.loop();
  ///SCHELUDED PUMP
  timeClient.update();
  const char* dayOfWeekChar = dayOfWeek(timeClient.getDay()).c_str();
  String scheduledTime = preferences.getString(dayOfWeekChar, "");
  if (scheduledTime != "") {
    String currentTime = String(timeClient.getHours()) + ":" + String(timeClient.getMinutes());
    if (currentTime == scheduledTime) {
      digitalWrite(pump, HIGH); // Activez le relais
      delay(3000);
    }
  }

  delay(1000);
  //Commande manuelle de la pompe
  command_manuel();

  ////Envoyer le niveau d'eau au serveur 
  waterLevel = distance();
  client.publish(TOPIC_LEVEL,String(waterLevel).c_str());
  delay(10);

  /////Envoyer la temperature au serveur 
  float temperature = Temperature();
  client.publish(TOPIC_TEMPERATURE, String(temperature).c_str());
  delay(1000);

  /////Envoyer l'humidité
  float humidite = Humidite();
  client.publish(TOPIC_HUMIDIDTE, String(humidite).c_str());
  delay(1000);  

  ///// Envoyer la luminosité 
  int luminosite = Luminosite();
  client.publish(TOPIC_LUMINOSITE, String(luminosite).c_str());
  delay(1000);
 
}
/////Distance en entier 
int distance(){
  unsigned int distance = sonar.ping_cm();
  if (distance)
  return distance;
}
/// Commande manuelle bouton poussoir 
void command_manuel(){
  int val = digitalRead(inputPin);  
  if (val == HIGH) {            
    digitalWrite(pump, HIGH);  
    Serial.print("pompe allumée");
    delay(3000);
  } else {
    digitalWrite(pump, LOW);
    
  }
}
/// recuperation de lhumidite
float Humidite() {
  float humidity = dht.readHumidity();
  if (isnan(humidity)) {
    Serial.println("Echec lecture humidité");
    return -1.0;
  }
  return humidity;
}
// recuperation temerature
float Temperature() {
  float temperature = dht.readTemperature();
  if (isnan(temperature)) {
    Serial.println("Echec lecture température");
    return -1.0;
  }
  return temperature;
}

int Luminosite(){
  int readLuminosite = analogRead(sensorPin);
  return readLuminosite; 
}
/////jour de la semaine
String dayOfWeek(int day) {
  switch (day) {
    case 1: return "lundi";
    case 2: return "mardi";
    case 3: return "mercredi";
    case 4: return "jeudi";
    case 5: return "vendredi";
    case 6: return "samedi";
    case 0: return "dimanche";
    default: return "";
  }
}

