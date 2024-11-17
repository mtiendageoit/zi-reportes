package com.zonainmueble.reports.conclusion;

import java.util.Map;

import com.zonainmueble.reports.utils.StringUtils;

public class ConclusionMessages {
  public static String systemMessage() {
    String message = """
        Eres un experto en bienes raices, por favor toma estos datos y realiza una conclusion con estilo neutral que pueda ayudar a tener una visión para poder comprar, rentar o invertir en la propiedad situada en la dirección dada. Tu respuesta debe ser consisa y facil de entender. En formato prosa por favor. Expresate de forma neutral con respecto a los datos.
        """;
    return message;
  }

  public static String basicoConclusionMessage(Map<String, Object> params) {
    String context = """
        La dirección del inmueble es [address].
        Es una [zona_habitantes] en comparación con los datos de [nombre_edo].
        [zona_habitantes_infantil] en comparación con la media de [nombre_edo] con rangos de 0 a 11 años.
        [_estudios1_]
        En la zona hay [habitantes_vivienda] habitantes por vivienda lo cual representa una [zona_densidad].
        [_pois1_][_pois2_]
        El número de habitantes es [habitantes], en 2010 habían [habitantes2010].
        Número de niños [ninos] de entre 0 y 11 años.
        La escolaridad promedio en la zona es de [zona_peh_grado_promedio_1] años equivalentes a [zona_peh_desc_1].
        Existen [familias] familias y [viviendas] viviendas de las cuales [viviendas_1_dormitorio] viviendas tienen 1 dormitorio, [viviendas_2mas_dormitorios] tienen 2 o más dormitorios.
        El área de estudio es un polígono de [isochrone_time_minutes] minutos [isochrone_transport_type].
        Dame una conclusión que no exceda las 100 palabras.
        """;

    String estudios1 = "Grado promedio de estudios no disponible para la zona";
    if (params.get("zona_peh_porcentaje_1") != null) {
      estudios1 = """
          El [zona_peh_porcentaje_1]% de los habitantes tienen [zona_peh_desc_1], en promedio en [nombre_edo] el [zona_peh_porcentaje_edo_1]% de sus habitantes tiene [zona_peh_desc_1].
          """;
    }

    String pois1 = "";
    if (params.get("pois_numero_1") != null) {
      pois1 = "En la zona existen [pois_numero_1] [pois_nombre_1]";
    }

    String pois2 = "";
    if (params.get("pois_numero_2") != null) {
      pois2 = ", y tambien [pois_numero_2] [pois_nombre_2].";
    }

    context = context.replace("[_estudios1_]", estudios1);

    context = context.replace("[_pois1_]", pois1);
    context = context.replace("[_pois2_]", pois2);

    String message = StringUtils.replaceKeysWithValues(context, params);
    return message;
  }

  public static String integralNivelBienestarPreponderantes(Map<String, Object> params) {
    String context = """
        [nse_tops_poblacion_porcentaje]% de la población tienen esta preponderancia de nivel de bienestar dentro del polígono de 15 minutos caminando.
        Con el [nse_top1_poblacion_porcentaje]% de la población que pertenece al nivel socio-económico [nse_top_1_nse] que pertenece a [nse_top_1_nombre] y tiene un ingreso mensual de aproximadamente [nse_top_1_desc].
        Y el [nse_top2_poblacion_porcentaje]% de la población que pertenece al nivel socio-económico [nse_top_2_nse] que pertenece a [nse_top_2_nombre] y tiene un ingreso mensual de aproximadamente [nse_top_2_desc].
        Dame una conclusión que no exceda las 80 palabras.
        """;

    String message = StringUtils.replaceKeysWithValues(context, params);
    return message;
  }

  public static String integralNivelBienestarZonaAnalisis(Map<String, Object> params) {
    String context = """
        Dentro del polígono de 5 minutos caminando existe una población total de [nse_iso5_poblacion] habitantes, el [nse_iso5_top_1_porcentaje]% tiene un nivel socio-económico
        [nse_top_1_nse] que es un nivel [nse_top_1_nombre] con ingresos mensuales promedio de [nse_top_1_desc]. El [nse_iso5_top_2_porcentaje]% tiene un nivel socio-económico
        [nse_top_2_nse] que es un nivel [nse_top_2_nombre] con ingresos mensuales promedio de [nse_top_2_desc], y el [nse_iso5_top_3_porcentaje]% tiene un nivel socio-económico
        [nse_top_3_nse] que es un nivel [nse_top_3_nombre] con ingresos mensuales promedio de [nse_top_3_desc].
        Para el polígono de 10 minutos caminando existe una población total de [nse_iso10_poblacion] habitantes, el [nse_iso10_top_1_porcentaje]% tiene un nivel socio-económico
        [nse_top_1_nse]. El [nse_iso10_top_2_porcentaje]% tiene un nivel socio-económico [nse_top_2_nse] y el [nse_iso10_top_3_porcentaje]% tiene un nivel socio-económico [nse_top_3_nse].
        Para el polígono de 15 minutos caminando existe una población total de [nse_iso15_poblacion] habitantes, el [nse_iso15_top_1_porcentaje]% tiene un nivel socio-económico
        [nse_top_1_nse]. El [nse_iso15_top_2_porcentaje]% tiene un nivel socio-económico [nse_top_2_nse] y el [nse_iso15_top_3_porcentaje]% tiene un nivel socio-económico [nse_top_3_nse].
        En general el ingreso mensual promedio de esta zona de análisis es de [nse_ingreso_promedio] pesos MXN.
        NOTA: Has una conclusión con esta información comenzando con la frase "Revela una notable" o "Denota...". Puedes generar máximo 2 parrafos con por lo menos un salto de linea, respetando el número máximo de palabras de 70.
        """;

    String message = StringUtils.replaceKeysWithValues(context, params);
    return message;
  }

  public static String integralConclusionMessage(Map<String, Object> params) {
    String context = """
        La dirección del inmueble es [address].
        1.-El perfil socio-demográfico de la zona indica la siguiente información en el área de estudio que es un polígono de [isochrone_time_minutes] minutos [isochrone_transport_type]:
        Es una [zona_habitantes] en comparación con los datos de [nombre_edo].
        [zona_habitantes_infantil] en comparación con la media de [nombre_edo] con rangos de 0 a 11 años.
        [_estudios1_]
        En la zona hay [habitantes_vivienda] habitantes por vivienda lo cual representa una [zona_densidad].
        [_pois1_][_pois2_]
        El número de habitantes es [habitantes], en 2010 habían [habitantes2010].
        Número de niños [ninos] de entre 0 y 11 años.
        La escolaridad promedio en la zona es de [zona_peh_grado_promedio_1] años equivalentes a [zona_peh_desc_1].
        Existen [familias] familias y [viviendas] viviendas de las cuales [viviendas_1_dormitorio] viviendas tienen 1 dormitorio, [viviendas_2mas_dormitorios] tienen 2 o más dormitorios.

        2.- El nivel de bienestar e ingresos de la zona indica la siguiente información:
        [nse_tops_poblacion_porcentaje]% de la población tienen esta preponderancia de nivel de bienestar dentro del polígono de 15 minutos caminando.
        Con el [nse_top1_poblacion_porcentaje]% de la población que pertenece al nivel socio-económico [nse_top_1_nse] que pertenece a [nse_top_1_nombre] y tiene un ingreso mensual de aproximadamente [nse_top_1_desc].
        Y el [nse_top2_poblacion_porcentaje]% de la población que pertenece al nivel socio-económico [nse_top_2_nse] que pertenece a [nse_top_2_nombre] y tiene un ingreso mensual de aproximadamente [nse_top_2_desc].
        En general el ingreso mensual promedio de esta zona de análisis es de [nse_ingreso_promedio] pesos MXN.

        3.- La piramide de maslow inmobiliaria de la zona indica la siguiente información:
        Nivel 1 (NECESIDADES BÁSICAS) que abarca tiendas, mercados, emergencias de salud y farmacias con un [piramide_maslow_porcentaje_nivel_1]% cubierto.
        Nivel 2(SEGURIDAD Y ESTABILIDAD) que abarca bancos,cajeros automáticos,transporte público,gasolineras,bomberos y policías con un [piramide_maslow_porcentaje_nivel_2]% cubierto.
        Nivel 3(ESPACIOS DE CONEXIÓN) que abarca super mercados, plazas y jardines, cafererías, restaurantes interesantes, clubs con un [piramide_maslow_porcentaje_nivel_3]% cubierto.
        Nivel 4(ESTILO Y PRESTIGIO) que abarca clubes, actividades deportivas, plazas comerciales, librerías y museos con un [piramide_maslow_porcentaje_nivel_4]% cubierto.
        Nivel 5(HORIZONTES PLENOS) que abarca espacios al aire libre, artes y cultura, espíritu con un [piramide_maslow_porcentaje_nivel_5]% cubierto.
        En general la zona de 15 minutos caminando cubre el [piramide_maslow_porcentaje_global]% de las necesidades generales del estudio de piramide de maslow inmobiliaria.

        4.- El perfil de movilidad y accesibilidad de la zona indica la siguiente información:
        Existen [poisNumeroGasolineras] gasolineras, la más cercana a [poisGasolineraCercana] metros.
        Existen [poisNumeroEstacionamientos] estacionamientos, el más cercano a [poisEstacionamientoCercano] metros.
        En análisis de movilidad en automovil indica que se puede viajar [isoAutomovilAsueto10] kilometros en 10 minutos, [isoAutomovilAsueto30] km en 30 minutos y [isoAutomovilAsueto60]km en 60 minutos manejando en un día de asueto (sin tráfico) como un domingo a las 8:00 am. En comparación con un día laboral normal (lunes 8:00 am) con los siguientes datos: [isoAutomovilLaboral10]km en 10 minutos, [isoAutomovilLaboral30] km en 30 minutos y [isoAutomovilLaboral60] kilometros en 60 minutos.

        NOTA: Has una conclusión con esta información que sea sencilla y facil de entender. Que ayude a una persona a tener una idea clara de la zona para que pueda tomar una desición informada para comprar, rentar o invertir en la propiedad. Puedes generar máximo 2 parrafos con por lo menos un salto de linea, respetando el número máximo de palabras de 150.
        """;

    String estudios1 = "Grado promedio de estudios no disponible para la zona";
    if (params.get("zona_peh_porcentaje_1") != null) {
      estudios1 = """
          El [zona_peh_porcentaje_1]% de los habitantes tienen [zona_peh_desc_1], en promedio en [nombre_edo] el [zona_peh_porcentaje_edo_1]% de sus habitantes tiene [zona_peh_desc_1].
          """;
    }

    String pois1 = "";
    if (params.get("pois_numero_1") != null) {
      pois1 = "En la zona existen [pois_numero_1] [pois_nombre_1]";
    }

    String pois2 = "";
    if (params.get("pois_numero_2") != null) {
      pois2 = ", y tambien [pois_numero_2] [pois_nombre_2].";
    }

    context = context.replace("[_estudios1_]", estudios1);

    context = context.replace("[_pois1_]", pois1);
    context = context.replace("[_pois2_]", pois2);

    String message = StringUtils.replaceKeysWithValues(context, params);
    return message;
  }
}
