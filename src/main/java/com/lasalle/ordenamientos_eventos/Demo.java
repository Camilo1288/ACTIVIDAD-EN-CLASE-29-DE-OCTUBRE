/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lasalle.ordenamientos_eventos;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
/**
 *
 * @author Usuario
 */
public class Demo {
    static Evento e(int id, int p) {
        return new Evento(id, Instant.parse("2026-01-01T00:00:00Z"),
                "Evento " + id, p);
    }

    static void verificar(String nombre, Consumer<Evento[]> ordenar,
            Comparator<Evento> criterio, boolean estable) {
        Random rnd = new Random(2024);
        for (int caso = 0; caso < 250; caso++) {
            int n = caso < 5 ? caso : rnd.nextInt(150);
            Evento[] a = new Evento[n];
            for (int i = 0; i < n; i++) a[i] = e(rnd.nextInt(500), rnd.nextInt(10));
            if (caso % 3 == 0) Arrays.sort(a, criterio);
            if (caso % 3 == 1) Arrays.sort(a, criterio.reversed());
            Evento[] esperado = a.clone();
            Arrays.sort(esperado, criterio);
            ordenar.accept(a);
            for (int i = 0; i < n; i++) {
                if (criterio.compare(a[i], esperado[i]) != 0)
                    throw new AssertionError(nombre + ": orden incorrecto");
                if (estable && a[i] != esperado[i])
                    throw new AssertionError(nombre + ": estabilidad");
            }
            Set<Evento> originales = Collections.newSetFromMap(new IdentityHashMap<>());
            originales.addAll(Arrays.asList(esperado));
            for (Evento item : a)
                if (!originales.remove(item)) throw new AssertionError("Permutacion");
            if (!originales.isEmpty()) throw new AssertionError("Perdida");
        }
        System.out.println(nombre + ": 250 casos OK");
    }

    // ---------- NUEVO: ordenamiento por fecha ----------
    static Evento eFecha(int id, int p, long horas) {
        return new Evento(id,
                Instant.parse("2026-01-01T00:00:00Z").plusSeconds(horas * 3600),
                "Evento " + id, p);
    }

    static void verificarFecha(String nombre, Consumer<Evento[]> ordenar, boolean estable) {
        Comparator<Evento> f = Ordenamientos.POR_FECHA;
        Random rnd = new Random(2024);
        for (int caso = 0; caso < 100; caso++) {
            int n = rnd.nextInt(150);
            Evento[] a = new Evento[n];
            for (int i = 0; i < n; i++)
                a[i] = eFecha(rnd.nextInt(500), rnd.nextInt(10), rnd.nextInt(50));
            Evento[] esperado = a.clone();
            Arrays.sort(esperado, f);
            ordenar.accept(a);
            for (int i = 0; i < n; i++) {
                if (f.compare(a[i], esperado[i]) != 0)
                    throw new AssertionError(nombre + " por fecha: orden incorrecto");
                if (estable && a[i] != esperado[i])
                    throw new AssertionError(nombre + " por fecha: estabilidad");
            }
        }
        System.out.println(nombre + " por fecha: 100 casos OK");
    }

    static void mostrar(String titulo, Evento[] a) {
        System.out.println(titulo + ":");
        for (Evento ev : a)
            System.out.println("   id " + ev.getId() + "  fecha " + ev.getFechaCreacion());
    }

    static Evento[] datosEjemplo() {
        return new Evento[] {
            eFecha(10, 2, 48),
            eFecha(11, 0, 5),
            eFecha(12, 1, 30),
            eFecha(13, 3, 1),
            eFecha(14, 2, 72)
        };
    }

    static void ejemploFecha(String nombre, Consumer<Evento[]> ordenar) {
        Evento[] a = datosEjemplo();
        System.out.println("== " + nombre + " por fecha ==");
        mostrar("Antes", a);
        ordenar.accept(a);
        mostrar("Despues", a);
    }
    // ---------- NUEVO: registros mock y tiempos ----------
static Evento[] generarMock(int n) {
    Random rnd = new Random(42);
    Evento[] datos = new Evento[n];
    for (int i = 0; i < n; i++)
        datos[i] = eFecha(i, rnd.nextInt(10), rnd.nextInt(10000));
    return datos;
}

static void medir(String nombre, String criterio, Evento[] base, Consumer<Evento[]> ordenar) {
    Evento[] copia = base.clone();
    long ini = System.nanoTime();
    ordenar.accept(copia);
    double ms = (System.nanoTime() - ini) / 1_000_000.0;
    System.out.println(String.format("%-10s (%-9s): %10.3f ms", nombre, criterio, ms));
}

static void medirTiempos(int n) {
    System.out.println("--- Tiempos con " + n + " registros mock ---");
    Evento[] base = generarMock(n);
    Comparator<Evento> f = Ordenamientos.POR_FECHA;
    medir("Bubble", "fecha", base, a -> Ordenamientos.bubble(a, f));
    medir("Selection", "fecha", base, a -> Ordenamientos.selection(a, f));
    medir("Insertion", "fecha", base, a -> Ordenamientos.insertion(a, f));
    medir("Merge", "fecha", base, a -> Ordenamientos.merge(a, f));
    medir("Quick", "fecha", base, a -> Ordenamientos.quick(a, f));
    medir("Heap", "fecha", base, a -> Ordenamientos.heap(a, f));
    medir("Shell", "fecha", base, a -> Ordenamientos.shell(a, f));
    medir("Counting", "prioridad", base, Ordenamientos::counting);
    medir("Bucket", "prioridad", base, Ordenamientos::bucket);
    medir("Radix", "id", base, Ordenamientos::radix);
}

    public static void main(String[] args) {
        Comparator<Evento> c = Ordenamientos.POR_PRIORIDAD;
        verificar("Bubble", a -> Ordenamientos.bubble(a,c), c, true);
        verificar("Selection", a -> Ordenamientos.selection(a,c), c, false);
        verificar("Insertion", a -> Ordenamientos.insertion(a,c), c, true);
        verificar("Merge", a -> Ordenamientos.merge(a,c), c, true);
        verificar("Quick", a -> Ordenamientos.quick(a,c), c, false);
        verificar("Heap", a -> Ordenamientos.heap(a,c), c, false);
        verificar("Counting", Ordenamientos::counting, c, true);
        verificar("Radix", Ordenamientos::radix,
                Comparator.comparingInt(Evento::getId), true);
        verificar("Bucket", Ordenamientos::bucket, c, true);
        verificar("Shell", a -> Ordenamientos.shell(a,c), c, false);
        Evento[] extremos = {e(Integer.MAX_VALUE,9), e(0,0), e(10,1)};
        Ordenamientos.radix(extremos);
        if (extremos[2].getId() != Integer.MAX_VALUE) throw new AssertionError();
        Evento[] todosIguales = new Evento[200];
        for (int i=0;i<200;i++) todosIguales[i]=e(i,3);
        Ordenamientos.quick(todosIguales,c);
        Evento[] ejemplo = {e(170,3),e(45,1),e(75,3),e(90,0),e(802,1),e(24,2)};
        Ordenamientos.counting(ejemplo);
        System.out.println(Arrays.toString(ejemplo));

        // ---------- NUEVO: ordenar por fecha ----------
        Comparator<Evento> f = Ordenamientos.POR_FECHA;
        verificarFecha("Bubble", a -> Ordenamientos.bubble(a, f), true);
        verificarFecha("Selection", a -> Ordenamientos.selection(a, f), false);
        verificarFecha("Insertion", a -> Ordenamientos.insertion(a, f), true);
        verificarFecha("Merge", a -> Ordenamientos.merge(a, f), true);
        verificarFecha("Quick", a -> Ordenamientos.quick(a, f), false);
        verificarFecha("Heap", a -> Ordenamientos.heap(a, f), false);
        verificarFecha("Shell", a -> Ordenamientos.shell(a, f), false);

        // Los 3 ejemplos
        ejemploFecha("Bubble", a -> Ordenamientos.bubble(a, f));
        ejemploFecha("Merge", a -> Ordenamientos.merge(a, f));
        ejemploFecha("Quick", a -> Ordenamientos.quick(a, f));
        
        medirTiempos(1000);
        medirTiempos(5000);
        medirTiempos(20000);
    }
    }

