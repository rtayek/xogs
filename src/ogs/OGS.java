package ogs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class OGS {
    static void pp(String jsonString) throws Exception {
        ObjectMapper objectMapper=new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // convert to java object
        Object jsonObject=objectMapper.readValue(jsonString,Object.class);
        String formattedJson=objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        System.out.println("pp: "+formattedJson);
    }
    static JsonNode getJSON(URL url) {
        HttpURLConnection connection;
        try {
            connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response=new StringBuilder();
            String line;
            while((line=reader.readLine())!=null) { response.append(line); }
            reader.close();
            connection.disconnect();
            ObjectMapper objectMapper=new ObjectMapper();
            JsonNode jsonNode=objectMapper.readTree(response.toString());
            Thread.sleep(200);
            return jsonNode;
        } catch(InterruptedException e) {
            System.out.println("caught: "+e);
        } catch(ProtocolException e) {
            System.out.println("caught: "+e);
        } catch(JsonMappingException e) {
            System.out.println("caught: "+e);
        } catch(JsonProcessingException e) {
            System.out.println("caught: "+e);
        } catch(IOException e) {
            System.out.println("caught: "+e);
            if(e.toString().contains("HTTP response code: 429")) {
                System.out.println("429! - going  to sleep.");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.out.println("429! - awake.");
            }
            //e.printStackTrace();
        }
        return null;
    }
    static void listReviews(List<Integer> playerIds,List<Integer> reviewers) {
        JsonNode games;
        for(Integer playerId:playerIds)
        try {
            System.out.println("playerId: "+playerId);
            games=getJSON(new URL("https://online-go.com/api/v1/players/"+playerId+"/games"));
            Integer page=1;
            String n=games.get("count").asText();
            System.out.println(n+" games.");
            String next=games.get("next").asText();
            //System.out.println("page "+page+" has next: "+next);
            outer:while(next!=null) {
                JsonNode gamesArray=games.get("results");
                // Get JSON array
                if(gamesArray.isArray()) {
                    for(JsonNode game:gamesArray) {
                        Integer gameId=game.get("id").asInt();
                        String started=game.get("started").asText();
                        String ended=game.get("ended").asText();
                        URL reviewsUrl=new URL("https://online-go.com/api/v1/games/"+gameId.toString()+'/'+"reviews");
                        //System.out.println("reviews url: "+reviewsUrl);
                        JsonNode reviews=getJSON(reviewsUrl);
                        if(reviews!=null) {
                            Integer count=reviews.get("count").asInt();
                            if(count==0) continue;
                            System.out.print("game: "+gameId+' '+game.get("name").asText());
                            System.out.print(" "+started+" - "+ended);
                            System.out.println();
                            JsonNode results=reviews.get("results");
                            if(results.isArray()) {
                                //System.out.println(reviews);
                                for(JsonNode review:results) {
                                    int reviewerId=review.get("id").asInt();
                                    JsonNode owner=review.get("owner");
                                    String username=owner.get("username").asText();
                                    int ownerId=owner.get("id").asInt();
                                    //System.out.println("\tgameId: "+gameId);
                                    //System.out.println("\treview: "+username);
                                    //System.out.println("\treview: "+review);
                                    //System.out.println("\townerId: "+"'"+ownerId+"'");
                                    //System.out.println("\treviewerId: "+"'"+reviewerId+"'");
                                    if(reviewers==null||reviewers.contains(ownerId)) {
                                        //System.out.println("\treview: "+review);
                                        System.out.println("\treview: "+reviewerId+" by: "+username);
                                    }
                                }
                            } else System.out.println("not an array!");
                            //System.out.println("Game ID: "+game.get("id").asInt());
                            //System.out.println("Game Name: "+game.get("name").asText());
                        } else {
                            System.out.println("reviews is null!");
                            System.out.println("probably due to a 429. so stopping.");
                            break outer;
                        }
                    }
                }
                // check for next page
                games=getJSON(new URL(next));
                //System.out.println("pp games: ");
                //pp(games.toString());
                next=games.get("next").asText();
                ++page;
                //System.out.println("page "+page+" has next: "+next);
            }
        } catch(MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String[] argumentss) {
        Integer rtayek=179,rtayek3=1386205,hugh=1567393;
        Integer mag11=1476738,otherpeter=1710964;
        Integer playerId;
        List<Integer> reviewers=null;
        List<Integer> playerIds=null;
        if(argumentss==null||argumentss.length==0) { //
            playerIds=Arrays.asList(mag11,otherpeter);
            reviewers=Arrays.asList(rtayek,rtayek3);
            
            System.out.println("reviews by: "+reviewers);
            listReviews(playerIds,reviewers);
        }
        for(String argument:argumentss) {
            playerId=Integer.valueOf(argument);
            System.out.println("reviews by: "+reviewers);
            playerIds=Arrays.asList(playerId);
            listReviews(playerIds,reviewers);
        }
    }
}
