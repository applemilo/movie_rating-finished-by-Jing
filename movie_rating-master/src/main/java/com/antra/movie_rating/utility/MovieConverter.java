package com.antra.movie_rating.utility;

import com.antra.movie_rating.domain.Movie;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


/**
 * @Author Jing
 * @Description
 *
 * Encapsoluated the fixed part for interacting with omdbapi
 *
 */
public class MovieConverter {

    private static final String url="http://www.omdbapi.com";
    private static final RestTemplate rt = new RestTemplate();


    public static Movie MovieConverterByTitle(String title){
        URI uri=UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("apikey", "27c4caaf").queryParam("plot", "full").queryParam("t",title).build().toUri();
        System.out.println(uri);

        RequestEntity<Void> request = RequestEntity
                .get(uri).accept(MediaType.APPLICATION_JSON).build();
        ResponseEntity<Movie> movie = rt.exchange(request, Movie.class);
        return movie.getBody();
    }

    public static Movie MovieConverterByImdbId(String imdbId){
        URI uri=UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("apikey", "27c4caaf").queryParam("plot", "full").queryParam("i",imdbId).build().toUri();
        RequestEntity<Void> request = RequestEntity
                .get(uri).accept(MediaType.APPLICATION_JSON).build();
        ResponseEntity<Movie> movie = rt.exchange(request, Movie.class);
        return movie.getBody();
    }

}
