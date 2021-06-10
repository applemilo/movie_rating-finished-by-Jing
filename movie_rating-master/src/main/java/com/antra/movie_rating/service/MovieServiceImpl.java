package com.antra.movie_rating.service;

import com.antra.movie_rating.api.request.MovieCriteria;
import com.antra.movie_rating.dao.MovieAverageScoreRepository;
import com.antra.movie_rating.dao.MovieDAO;
import com.antra.movie_rating.domain.Movie;
import com.antra.movie_rating.domain.MovieAverageScore;
import com.antra.movie_rating.utility.MovieConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MovieServiceImpl.class);

	@Autowired
	MovieDAO movieDAO;

	@Autowired
	MovieAverageScoreRepository avgScoreDAO;



	@Override
	@Cacheable(key="#criteria.title", value="movieCache", sync = true)
	@Transactional
	public Movie searchMovie(MovieCriteria criteria) {
		List<Movie> res = movieDAO.findByTitleIgnoreCase(criteria.getTitle());
		if (res.size() > 0) {
			return res.get(0);
		}


		Movie movie= MovieConverter.MovieConverterByTitle(criteria.getTitle());

		LOGGER.info(movie.toString());
		if (movieDAO.findByImdbIdIgnoreCase(movie.getImdbId()) == null) {
			movieDAO.save(movie);
		}

		return movie;
	}

	/**
	 * @Author Jing
	 *search and save using this api "http://www.omdbapi.com",
	 * since our database don't have enough data
	 */


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

	/**
	 * @Author Jing
	 * @param type filter by type in popular list
	 * @return can test Popular Action or Carton(it should be Animation)
	 */
	@Override
	@Cacheable(value="populartypeMovieCache", sync = true)
	public List<Movie> searchMovie(String type) {
		List<Movie> popularMovies=searchMovie();

		List<Movie> result=popularMovies.stream().filter(m->m.getGenre().toLowerCase().contains(type.toLowerCase())).collect(Collectors.toList());
		return result;

	}


//	@Override
//	@Cacheable(value="popularMovieCache", sync = true)
//	public List<Movie> searchMovie(String type) {
//		List<Movie> result = new ArrayList<>();
//		switch (type) {
//			case "popular":
//				result.add(this.searchMovie(new MovieCriteria("flight")));
//				result.add(this.searchMovie(new MovieCriteria("titanic")));
//				result.add(this.searchMovie(new MovieCriteria("Hannibal")));
//				result.add(this.searchMovie(new MovieCriteria("The Fast and the Furious")));
//				result.add(this.searchMovie(new MovieCriteria("Pokemon: Pikachu's Rescue Adventure")));
//				break;
//			case "cartoon":
//				result.add(this.searchMovie(new MovieCriteria("tangled")));
//				result.add(this.searchMovie(new MovieCriteria("frozen")));
//				result.add(this.searchMovie(new MovieCriteria("uglydolls")));
//				result.add(this.searchMovie(new MovieCriteria("zootopia")));
//				break;
//			case "action":
//				result.add(this.searchMovie(new MovieCriteria("rush hour")));
//				result.add(this.searchMovie(new MovieCriteria("Avengers: endgame")));
//				result.add(this.searchMovie(new MovieCriteria("black panther")));
//				result.add(this.searchMovie(new MovieCriteria("Captain Marvel")));
//				result.add(this.searchMovie(new MovieCriteria("black panther")));
//				break;
//		}
//
//		return result;
//	}

	@Override
	@Transactional(readOnly = true)
	public MovieAverageScore getMovieAverageScoreById(Integer id) {
		return avgScoreDAO.findByMovieId(id);
	}

	@Override
	public List<Movie> searchAllMovies() {
		return movieDAO.findAll();
	}

}
