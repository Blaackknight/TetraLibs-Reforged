package fr.bck.tetralibs.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marqueur pour qu’un module soit auto-détecté par TetraLibs. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoTetraModule {
    /** Valeur par défaut à écrire dans le fichier TOML si la clé n’existe pas. */
    boolean def() default true;
}