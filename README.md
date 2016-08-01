# PopularMovies
### The Movie DB API Key
In build.gradle (Module:app), 
`buildTypes.each {
  it.buildConfigField 'String', 'THEMOVIEDB_API_KEY', 'THEMOVIEDB_API_KEY'
}`,
please use api key instead of 'THEMOVIEDB_API_KEY'.
