package com.byron.screenmatch.principal;

import com.byron.screenmatch.model.DatosEpisodio;
import com.byron.screenmatch.model.DatosSerie;
import com.byron.screenmatch.model.DatosTemporadas;
import com.byron.screenmatch.model.Episodio;
import com.byron.screenmatch.service.ConsumoAPI;
import com.byron.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private final String URL = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=92e7899";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraElMenu()

    {
        System.out.println("Ingrese el nombre de la serie a buscar");
        var nombreSerie = sc.nextLine();
        var json = consumoAPI.obtenerDatos(URL + nombreSerie.replace(" ", "+") + APIKEY);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos);

        List<DatosTemporadas> temporadas = new ArrayList<>();

        for (int i = 1; i <= datos.totalDeTemporadas(); i++) {
            json = consumoAPI.obtenerDatos(URL + nombreSerie.replace(" ", "+") + "&Season=" + i + APIKEY);
            DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporada);
        }
        temporadas.forEach(System.out::println);
/*
        for (int i = 0; i < datos.totalDeTemporadas(); i++) {
            List<DatosEpisodio> episodiosTemporadas = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporadas.size(); j++) {
                System.out.println(episodiosTemporadas.get(j).titulo());
            }
        }
        */

        //temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));


        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t->t.episodios().stream())
                .collect(Collectors.toList());

    /*
        System.out.println("Top 5 episodios");
        datosEpisodios.stream()
                .filter(e->!e.evaluacion().equalsIgnoreCase("N/A"))
                .peek(e-> System.out.println("Primer Filtro"+ e))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(e-> System.out.println("Segunda Ordenación"+ e))
                .map(e ->e.titulo().toUpperCase())
                .peek(e-> System.out.println("Tercer Filtro"+ e))
                .limit(5)
                .forEach(System.out::println);


    */

        //Convertir los datos a una lista de tipo episodio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t->t.episodios().stream()
                        .map(d->new Episodio(t.numero(),d)))
                        .collect(Collectors.toList());
        //episodios.forEach(System.out::println);

        //Búsqueda de eoisodio a partir de x año
    /*
        System.out.println("Indique el año a partir del cual deseas ver los episodios");
        var fecha = sc.nextInt();
        sc.nextLine();
        LocalDate fechaDeBusqueda = LocalDate.of(fecha,1,1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e-> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaDeBusqueda))
                .forEach(e->
                    System.out.println("Temporada: " + e.getTemporada()+
                    " Episodio: "+ e.getTitulo()+
                            " Fecha de Lanzamiento: "+e.getFechaDeLanzamiento().format(dtf)
                    ));



    //Busca episodio por título
        System.out.println("Ingrese el titulo del episodio que desea buscar");
        var seccionTitulo = sc.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(seccionTitulo.toUpperCase()))
                .findFirst();
        if (episodioBuscado.isPresent()){
            System.out.println("Episodio Encontrado");
            System.out.println("Los datos son: "+episodioBuscado.get());
        }else{
            System.out.println("Episodio no encontrado");
        }
       */



        Map<Integer,Double> evaluacionPorTemporada = episodios.stream()
                .filter(e->e.getEvaluacion() >0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                                Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(evaluacionPorTemporada);



        DoubleSummaryStatistics est = episodios.stream()
                .filter(e->e.getEvaluacion() >0.0)
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        System.out.println("Media: "+est.getAverage()+
                " Mejor Evaluado: "+est.getMax()+
                " Peor Evaluado "+est.getMin());
   }


}
