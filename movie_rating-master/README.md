## To do List



### No UI changes

- Implement a feature to allow user search by `IMDB` or title.(use the existing search by title field. No UI changes.)
- Implement a feature to list all popular movies when `Popular` button is clicked on home page.(for now it is hard coded. No UI changes.)
- Implement a feature to list all popular Action / Cartoon movies. (for now it is hard coded. No UI changes.)
- Add cache expiration.(need to do some research. No UI changes.)



### What I did

I finished all the No UI changes. About UI changes, I am not so familiar with angular, the most front-end framework I used is Vue.js.

* Implement a feature to allow user search by `IMDB`

  ```java
  //consider our database is empty so need to get the data from another api, we need to reuse these fixed steps, so I encapsolated it into a unitil class
  
  
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
  
  
  
  
  //controller layer
  
  
  /**
  	 * @Author Jing
  	 * @param imdbID unique value
  	 *
  	 * @return if it exists, it will just return one value
  	 */
  	@GetMapping("/movie/imdbID")
  	public MovieVO getMovieByImdbID(@RequestParam String imdbID){
  
  		Movie movie=movieService.searchMovieByImdbID(imdbID);
  
  		if (movie==null) {
  			throw new MovieNotExistExeption();
  		}
  		MovieVO resp = new MovieVO(movie);
  
  		return resp;
  	}
  
  //service layer
  //interface
  Movie searchMovieByImdbID(String imdbId);
  
  
  //interface imp
  @Override
  	@Cacheable(value="movieCache", sync = true)
  	@Transactional
  	public Movie searchMovieByImdbID(String imdbId) {
  		Movie res = movieDAO.findByImdbIdIgnoreCase(imdbId);
  		if(res!=null) return res;
  
  		Movie movie=MovieConverter.MovieConverterByImdbId(imdbId);
  		movieDAO.save(movie);
  
  		LOGGER.info(movie.toString());
  
  
  		return movie;
  	}
  ```

  ![1.png](https://github.com/applemilo/Xiong/blob/master/1.png?raw=true)



* Implement a feature to list all popular movies when `Popular` button is clicked on home page.

  ```java
  //controller layer
  @GetMapping("/movie/popular")
  	public MovieListResp getPopularMovies(){
  		List<Movie> movies = movieService.searchMovie();
  		if (movies.size() < 1) {
  			throw new MovieNotExistExeption();
  		}
  		MovieListResp resp = new MovieListResp();
  		resp.setMovies(movies.stream().map(m->new MovieVO(m)).collect(Collectors.toList()));
  		return resp;
  	}
  
  //service layer
  //interface
  	List<Movie> searchMovie();
  
  
  //interface imp
  /**
   * @Author Jing
   *  I just simply define the standard of popular with average scores over 6
   *
   */
  @Override
  @Cacheable(value="popularMovieCache", sync = true)
  public List<Movie> searchMovie() {
     List<MovieAverageScore> allMovies = avgScoreDAO.findAll();
     //lazy initialization issue
     List<Movie> result=allMovies.stream()
           .filter(ma->ma.getAverageScore()>6).map(ma->ma.getMovie())
           .collect(Collectors.toList());
  
     return result;
  }
  
  //DAO layer, need to add one more interface
  public interface MovieAverageScoreRepository extends JpaRepository<MovieAverageScore, Integer>, MovieAverageScoreCustomRepo {
  	MovieAverageScore findByMovieId(Integer id);
  	//to help getting the rated movie
      List<MovieAverageScore> findAll();
  }
  
  
  ```

![2.png](https://github.com/applemilo/Xiong/blob/master/2.png?raw=true)

 * Implement a feature to list all popular Action / Cartoon movies.

   ```java
   //controller layer
   @GetMapping("/movie/popular/type")
   	public MovieListResp getMovieTypes(@RequestParam String type){
   		List<Movie> movies = movieService.searchMovie(type);
   		if (movies.size() < 1) {
   			throw new MovieNotExistExeption();
   		}
   		MovieListResp resp = new MovieListResp();
   		resp.setMovies(movies.stream().map(m->new MovieVO(m)).collect(Collectors.toList()));
   		return resp;
   	}
   
   //service layer
   //interface
   	List<Movie> searchMovie(String type);
   
   
   //interface imp
   /**
   	 * @Author Jing
   	 * @param type filter by type in popular list
   	 * @return can test Popular Action or Carton(it should be Animation)
   	 */
   	@Override
   	@Cacheable(value="populartypeMovieCache", sync = true)
   	public List<Movie> searchMovie(String type) {
   		List<Movie> popularMovies=searchMovie();
   //.tolowercase() helps user input ignore the case
   		List<Movie> result=popularMovies.stream().filter(m->m.getGenre().toLowerCase().contains(type.toLowerCase())).collect(Collectors.toList());
   		return result;
   
   	}
   ```

   ![3.png](https://github.com/applemilo/Xiong/blob/master/3.png?raw=true)

* Add cache expiration

  ```java
  //add dependcy
  		<dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-data-redis</artifactId>
          </dependency>
              
  //set configuration at application.properties
              
  ##redis
  spring.redis.port=6379
  
  spring.redis.host=127.0.0.1
  
  spring.redis.database=0
  
  spring.redis.password=
  
  spring.redis.jedis.pool.max-active=8
  
  spring.redis.jedis.pool.max-wait=-1ms
  
  spring.redis.jedis.pool.max-idle=8
  
  spring.redis.jedis.pool.min-idle=0
  
  spring.redis.timeout=5000ms
  
   //use config class to set a cacheManager
              
      @Configuration
      @EnableCaching
      public class RedisConfig implements Serializable {
           @Autowired
          private RedisTemplate<String, Object> template;
          @Bean
          public CacheManager cacheManager( ) {
  
              // configuration
              RedisCacheConfiguration defaultCacheConfiguration =
                      RedisCacheConfiguration
                              .defaultCacheConfig()
                              // set key is string
                              .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(template.getStringSerializer()))
                              // Object convert json into object
                              .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(template.getValueSerializer()))
                              // no cache is null
                              .disableCachingNullValues()
                              // cache data for 1 hour
                              .entryTtl(Duration.ofHours(1));
  
              // create a redis cache manager
              RedisCacheManager redisCacheManager =
                      RedisCacheManager.RedisCacheManagerBuilder
                              // Redis connection
                              .fromConnectionFactory(template.getConnectionFactory())
                              // cache setting
                              .cacheDefaults(defaultCacheConfiguration)
                              // put/evict
                              .transactionAware()
                              .build();
  
              return redisCacheManager;
          }
  
  
      }
  
              
  ```

  





### Bug fixed

![4.png](https://github.com/applemilo/Xiong/blob/master/4.png?raw=true)

The original swagger configuration just provide user principle to finish second step authorization, but the token need to insert in the header to finish the first step authorization.

```java
// use this global parameters inserting in header to help finishing the authorization
		ParameterBuilder tokenPar = new ParameterBuilder();
		List<Parameter> pars = new ArrayList<Parameter>();
		tokenPar.name("Authorization").description("Bearer token")
				.modelRef(new ModelRef("string")).parameterType("header")
				.required(false).build();
		pars.add(tokenPar.build());

		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.antra.movie_rating"))
//				.paths(regex("/api.*"))
//				.paths(regex("/movie.*"))
				.paths(PathSelectors.any())
				.build().apiInfo(metaInfo()).globalOperationParameters(pars);
```



![5.png](https://github.com/applemilo/Xiong/blob/master/5.png?raw=true)



The table structure of `MOVIE_RATING` is not correct 

![6.png](https://github.com/applemilo/Xiong/blob/master/6.png?raw=true)

h2 database set a extra foreign key pointer `MovieRating` itself. Since we already mapped by one to many relationship, that will cause insert value exception since we have a not null key but cannot be initialization

![7.png](https://github.com/applemilo/Xiong/blob/master/7.png?raw=true)



In the converting user input `ratingVO` to `MovieRating` part, we should initialize a user. When I test this case, I found the bug about the user's override `hashcode()`

![8.png](https://github.com/applemilo/Xiong/blob/master/8.png?raw=true)

The original  `hashcode()` when user just initialization by user ID will cause null pointer exception, and considering test User user just using id to finish initialization I add id.hashCode() to create different user instance, otherwise, all hashCode() equals 0.

![9.png](https://github.com/applemilo/Xiong/blob/master/9.png?raw=true)

