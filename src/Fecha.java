public class Fecha {
    private int dia;
    private int mes;
    private int año;
    public Fecha(int dia, int mes, int año){
        if (fechaValida(dia, mes, año)) {
            this.dia = dia;
            this.mes = mes;
            this.año = año;
        } else {
            System.out.println("Debe ingresar una fecha válida");
        }
    }
    public boolean fechaValida(int dia, int mes, int año) {
        if (año <= 0) {
            return false;
        }
        if (mes < 1 || mes > 12) {
            return false;
        }
        if (dia < 1) {
            return false;
        }
        if (mes == 2) {
            if (dia > 28) {
                return false;
            }
        } 
        else if (mes == 4 || mes == 6 || mes == 9 || mes == 11) {
            if (dia > 30) {
                return false;
            }
        } 
        else {
            if (dia > 31) {
                return false;
            }
        }
        return true;
    }
    public boolean otraFecha(Fecha otraFecha) {
        boolean esProxima = false;
        int fechaActual = this.dia + this.mes * 30 + this.año * 365;
        int fechaProxima = otraFecha.getDia() + otraFecha.getMes() * 30 + otraFecha.getAño() * 365;
        if ((fechaProxima - fechaActual) > 0) {
            esProxima = true;
        }
        return esProxima;
    }
    public String mostrarFecha() {
        return dia + "/" + mes + "/" + año;
    }
    public int getDia() {
        return dia;
    }
    public int getMes() {
        return mes;
    }
    public int getAño() {
        return año;
    }

    public static Fecha hoy() {
        java.time.LocalDate h = java.time.LocalDate.now();
        return new Fecha(h.getDayOfMonth(), h.getMonthValue(), h.getYear());
    }

    public static Fecha desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            String[] p;
            int dia, mes, año;
            if (texto.contains("-")) {
                p = texto.trim().split("-");
                año = Integer.parseInt(p[0]);
                mes = Integer.parseInt(p[1]);
                dia = Integer.parseInt(p[2]);
            } else {
                p = texto.trim().split("/");
                dia = Integer.parseInt(p[0]);
                mes = Integer.parseInt(p[1]);
                año = Integer.parseInt(p[2]);
            }
            Fecha f = new Fecha(1, 1, 2000);
            if (!f.fechaValida(dia, mes, año)) {
                return null;
            }
            return new Fecha(dia, mes, año);
        } catch (Exception e) {
            return null;
        }
    }

    public String aTexto() {
        return dia + "/" + mes + "/" + año;
    }
}
