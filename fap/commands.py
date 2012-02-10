#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os, os.path
import sys
import subprocess
import shutil
import re
import getopt
from datetime import date

# Here you can create play commands that are specific to the module, and extend existing commands

MODULE = 'fap'

# Commands that are specific to your module

COMMANDS = ['fap:hello', 'fap:generate', 'fap:init', 'fap:version', 'fap:documentation', 'fap:dist', 'fap:winservice', "fap:copyplatinoconf"]
# Eliminamos el comando 'fap:model' de la lista de comandos

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")
       
    if command == "fap:hello":
        print "~ Hello"

    if command == "fap:generate":
        versionASCIIART(app, args)
        run_generate(app, args)
        
#    if command == "fap:model":
#        run_model(app, args)
            
    if command == "fap:init":
        versionASCIIART(app, args)
        init_application (app, args)

    if command == "fap:version":
        version(app, args)
        versionASCIIART(app, args)
        
    if command == "fap:documentation":
        generateDocumentationHTML(app)

    if command == "fap:dist":
        dist(app, args)

    if command == "fap:winservice":
        winservice(app, args)    
    
    if command == "fap:copyplatinoconf":
        copyplatinoconf(app, args)

def version (app, args):
    depsYaml = os.path.join(getModuleDir(app, args), 'conf/dependencies.yml')
    if os.path.exists(depsYaml):
        deps = open(depsYaml).read()
        try:
             moduleDefinition = re.search(r'self:\s*(.*)\s*', deps).group(1)
             print moduleDefinition
        except Exception:
             pass

def versionASCIIART (app, args):
   readmeFile = os.path.join(getModuleDir(app, args), 'README'); 
   if os.path.exists(readmeFile):
      FILE = open(readmeFile).read();
      print FILE;  
  
        
def execute_workflow(modelPath, targetPath, params, cmd_args, app):
    moduleDir = getModuleDir(app, cmd_args)

    if(moduleDir == None):
        print 'No se encontro la ruta del modulo'
        sys.exit()
    
    generatorDir = os.path.join(moduleDir, 'compiler')
    generatorLibDir = os.path.join(generatorDir, 'lib')
    jars = []
    

    #Librerías
    if("--dev" in cmd_args):    
        #Desarrollo
        jars.append(os.path.join(moduleDir, "compiler/lib/*")) 
        jars.append(os.path.join(moduleDir, "compiler/src/es.fap.simpleled.generator/bin/"))
        jars.append(os.path.join(moduleDir, "compiler/src/es.fap.simpleled/bin/"))
    else:
        #No desarrollo
        jars.append(os.path.join(moduleDir, "compiler" ,"lib/*")) 
        jars.append(os.path.join(moduleDir, "compiler", "compiled/*")) 
        
    #Variables para ejecutar el script  
    separador = ':'
    if os.name == 'nt':
        separador = ';'
    
    classpath = separador.join(str(x) for x in jars)

    if("--debug" in cmd_args):
        log = "file:///" + os.path.join(generatorDir, "config", "log4-debug.properties")
    else:
        log = "file:///" + os.path.join(generatorDir, "config", "log4.properties")
    
    os.chdir(generatorDir);
    class_name = "org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher"
    
    fapModelPath = os.path.join(moduleDir, "app", "led");
    workflow = "workflow.LedGenerator";
    
    cmd = [app.java_path(), "-Dfile.encoding=utf-8","-classpath", classpath, class_name, workflow, "-p", "targetPath=" + targetPath, "modelPath=" + modelPath, "fapModelPath=" + fapModelPath, params];
    return subprocess.call(cmd);
        
    

def run_generate(app, args):
    modelPath = os.path.join(app.path, "led")
    targetPath =  app.path + "/"
    params = "solicitud=true"
    exit(execute_workflow(modelPath, targetPath, params, args, app))

def run_model(app, args):
    modelPath = os.path.join(os.getenv("FAPSDK"), "fap", "app", "led", "fap")
    targetPath =  os.path.join(os.getenv("FAPSDK"), "fap/")
    params = "solicitud=false"
    exit(execute_workflow(modelPath, targetPath, params, args, app))

# Para comprobar si en el sistema (WINDOWS) existe un proceso con un determinado
# PID y un determinado NAME
def exist_process (pid, name):
    # Comando que filtra los servicios arrancados con un determinado PID y NAME
    cmd = ["tasklist",  "/FI",  "PID eq "+pid, "/FI",  "IMAGENAME eq "+name]
    # Creamos una expresion regular con el formato de la salida que dará la consola
    # al no encontrar el servicio con el PID y NAME que queremos
    regexp = re.compile("^INFORMACI.*")
    # Ejecutamos el comando redirigiendo la salida a una variable para poder parsearla
    output = subprocess.Popen(cmd, stdout=subprocess.PIPE) 
    # Si la salida coincide con que no encontró ningun servicio con el PID y NAME que queremos
    # Devolvemos FALSE 
    if regexp.match(output.communicate()[0]):
        return False
    # Si, si existe el servicio arrancado con ese PID y ese NAME
    # Devolvemos TRUE
    else:
        return True

# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    if command == "dependencies":
        args.append('-Dfapsdk='+os.getenv("FAPSDK"))
        env = kargs.get("env")
    # Para que no se quede colgado el server.pid, tras ocurrir alguna incidencia con el servicio
    if command == "start":
        # Path del ficherito que play utiliza para saber el PID del servicio que arranca
        serverpidPath = app.path+"\server.pid"
        # Si existe dicho ficherito, pues comprobamos que no sea por una incidencia
        if os.path.exists(serverpidPath):
            # Leemos el PID que contiene dicho ficherito
            serverpid = open(serverpidPath, "r")
            pid = serverpid.read()
            # Cerramos el ficherito, por si despues hay que borrarlo
            serverpid.close()
            # Si no existe el proceso con el PID del ficherito y NAME de un proceso java
            # De esta manera filtramos algun servicio que haya podido coger justamente el mismo PID
            # que tenía nuestro servicio play, antes de quedarse 'colgado'
            # NOTA: Es verdad que de esta forma puede darse la circunstancia de que un servicio java y que
            # no se play, puede coger el mismo PID que se quedó cuando sucedió la incidencia. Pero asumimos
            # el riesgo de que eso ocurra.
            if not exist_process(pid, "java.exe"):
                # Eliminamos el ficherito, para que play pueda arrancar el servicio correctamente tras la incidencia
                os.remove(serverpidPath)
            # Si existe un proceso que play ya arranco con ese PID y ese NAME
            # En este caso puede ser por dos circunstacias (una controlada y otra que no es deseable):
            #    - DESEABLE: que ya se haya arrancado el proceso, y estemos haciendo otra vez un 'play start'.
            #                En este caso, Play además nos mostrará un mensajito de que el servicio ya está arrancado previamente
            #    - NO DESEABLE: que otro proceso que no sea el de Play, pero que sea de java, haya cogido el mismo PID que tenía
            #                   el servicio de Play anteriormente a la incidencia. Este caso debemos asumir que pueda ocurrir, 
            #                   y lo que sucederá es que Play no podrá arrancar el servicio. La única forma es borrando el ficherito
            #                   a mano.
            else:
                # Mostramos un mensajito diciendo que ya existe dicho proceso, como comentamos antes
                print "~ Ya existe un proceso con PID: "+pid
            

# This will be executed after any command (new, run...)
def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "new":
        print "Ejecutando new"
        
def getModuleDir(app, cmd_args=""):
    if("--dev" in cmd_args):
        if(os.getenv("FAPSDK") == None):
            print "Modo desarrollo (--dev) y la variable de entorno FAPSDK no está definida"
            sys.exit()  

        return os.path.join(os.getenv("FAPSDK"), "fap")    
    else:    
        if app.path and os.path.exists(os.path.join(app.path, 'modules')):
            regexp = re.compile("^fap(-(\d*\.*)*)?$")
            regexpNigthly = re.compile("^fap(-nb-(\d*\.*)*)?$")
            for m in os.listdir(os.path.join(app.path, 'modules')):
                mf = os.path.join(os.path.join(app.path, 'modules'), m)
                base = os.path.basename(mf)
                if regexp.match(base):
                    if os.path.isdir(mf):
                        return mf
                    else:
                        return open(mf, 'r').read().strip()
                # Nightly build regexp
                if regexpNigthly.match(base):
                    if os.path.isdir(mf):
                        return mf
                    else:
                        return open(mf, 'r').read().strip()
    return None        

def init_application (app, args):
    srcDir = os.path.join(app.path, "led", "src")
   
    print "Creando el esqueleto basico de una aplicacion FAP "
    moduleDir =  getModuleDir(app, args)
    
    conf = os.path.join(app.path, "conf", "application.conf");
    print "Se extiende application.conf con la configuracion necesaria";
    FILE = open(conf, "a");
    FILE.write("\n#FAP Configuration\n");
    FILE.write("fap.app.name=" + app.name() + "\n");
    FILE.write("#fap.ctxPath=\n");
    FILE.write("date.format=dd/MM/yyyy\n");
    FILE.write("fap.login.type.user=true\n");
    
    # Configuración de los Loggers
    FILE.write("%test.app.log.path=/log4j-test.properties");
    FILE.write("%prod.application.log.path=/log4j-prod.properties");
    FILE.write("application.log.path=/log4j-dev.properties");
    FILE.write("db=mem\n");
    FILE.write("%prod.jpa.ddl=none     # La primera vez que lo ejecutes debes ponerlo a 'create'" + "\n");
    
    
    FILE.write("\n\n# === FAPGENERATED ===\n");
    FILE.write("# === END FAPGENERATED ===\n");
    FILE.close();
   

    log4jFile_a = os.path.join(moduleDir, "conf", "log4j-dev.properties");
    log4jFile_b = os.path.join(app.path, "conf", "log4j-dev.properties");
    shutil.copy2(log4jFile_a, log4jFile_b);

    log4jFile_a = os.path.join(moduleDir, "conf", "log4j-prod.properties");
    log4jFile_b = os.path.join(app.path, "conf", "log4j-prod.properties");
    shutil.copy2(log4jFile_a, log4jFile_b);
    
    log4jFile_a = os.path.join(moduleDir, "conf", "log4j-test.properties");
    log4jFile_b = os.path.join(app.path, "conf", "log4j-test.properties");
    shutil.copy2(log4jFile_a, log4jFile_b);
    
    shutil.rmtree(os.path.join(app.path, "app", "views"), ignore_errors=True)
    shutil.rmtree(os.path.join(app.path, "app", "controllers"), ignore_errors=True)
    os.makedirs(os.path.join(app.path, "app", "views"));
    os.makedirs(os.path.join(app.path, "app", "controllers"));
    
    # Copia todo lo que hay dentro de fap/fap-skel a la nueva aplicacion
    copy_directory(os.path.join(moduleDir, "fap-skel"),  app.path);
    
    # Para la documentacion de la aplicacion en HTML
    os.makedirs(os.path.join(app.path, "documentation"));
    os.makedirs(os.path.join(app.path, "documentation", "html"));
    os.makedirs(os.path.join(app.path, "documentation", "html", "plantillas"));
    copy_directory(os.path.join(moduleDir, "documentation/html/plantillas"),  app.path+"/documentation/html/plantillas");
    
    # Para las carpetas de backups de los logs
    os.makedirs(os.path.join(app.path, "logs", "backups"));
    os.makedirs(os.path.join(app.path, "logs", "backups", "Auditable"));
    os.makedirs(os.path.join(app.path, "logs", "backups", "Daily"));
    
def copy_directory(source, target):
    if not os.path.exists(target):
        os.mkdir(target)
    for root, dirs, files in os.walk(source):
        if '.svn' in dirs:
            dirs.remove('.svn')  # don't visit .svn directories
        for file in files:
            from_ = os.path.join(root, file)
            to_ = from_.replace(source, target, 1)
            
            to_directory = os.path.split(to_)[0]
            if not os.path.exists(to_directory):
                os.makedirs(to_directory)
            print "Creando el fichero " + to_
            shutil.copyfile(from_, to_)    
    
def generateDocumentationHTML(app):
    print "~ Generando la documentacion ..."
    # Accedo a la carpeta donde estan los ficheros
    ruta_app = app.path.replace("\\", "/")+"/led"
    ruta_modulo = getModuleDir(app, "")
    ruta_ledFap = ruta_modulo.replace("\\", "/")+"/app/led/fap"
    ruta_htmlDoc = ruta_modulo.replace("\\", "/")+"/documentation/html"
    ruta_clase= ruta_modulo+"\\compiler\\gendocumentation\\bin"
    ruta_plantilla = ruta_modulo.replace("\\", "/")+"/compiler/gendocumentation"
    class_name = "GenerarDocumentacionHTML"
    regexp = re.compile(".fap$")
    # Primero creo la documentacion de los fichero "*.fap" que vienen por defecto
    ficheros = os.listdir(ruta_ledFap)
    for f in ficheros:
        if (regexp.search(f)): # Si es un fichero "*.fap", creo su documentacion
            fuente = ruta_ledFap+"\\"+f
            # Nombre del fichero destino de la documentacion
            nombreDoc = f.replace(".fap", "FAPDocumentacion.html")
            destino = ruta_htmlDoc+"/"+nombreDoc
            # Por cada fichero ejecutamos la generacion de su documentacion
            classpath=ruta_clase+";"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\groovy-all-1.7.5.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-textile.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-wikitext.jar"
            cmd = [app.java_path(), "-Dfile.encoding=utf-8","-classpath", classpath, class_name, fuente, destino, ruta_plantilla, ruta_modulo.replace("\\", "/"), nombreDoc];
            subprocess.call(cmd)
            primero="2"
            print "~ [CREADO]: "+destino
    # Recorro la carpeta en busca de los fichero "*.fap", propios del proyecto
    ficheros = os.listdir(ruta_app)
    for f in ficheros:
        if (regexp.search(f)): # Si es un fichero "*.fap", creo su documentacion
            fuente = ruta_app+"\\"+f
            # Nombre del fichero destino de la documentacion
            nombreDoc = f.replace(".fap", "Documentacion.html")
            destino = app.path.replace("\\", "/")+"/documentation/html"+"/"+nombreDoc
            # Por cada fichero ejecutamos la generacion de su documentacion
            classpath=ruta_clase+";"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\groovy-all-1.7.5.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-textile.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-wikitext.jar"
            cmd = [app.java_path(), "-Dfile.encoding=utf-8","-classpath", classpath, class_name, fuente, destino, ruta_plantilla, ruta_modulo.replace("\\", "/"), nombreDoc];
            subprocess.call(cmd)
            print "~ [CREADO]: "+destino



def copytree(source, dest, ignores):
   shutil.copytree(source, dest, ignore=shutil.ignore_patterns(*ignores))

def makeDirsIfNotExists(source):
   if not os.path.exists(source):
      os.makedirs(source)

def dist(app, args):
   # Precompila
   # TODO ver si se puede cambiar por la llamada directa a la clase
   # para que no aparezca otra vez el logo de play
   ret = subprocess.call(["play.bat", "precompile"]);
   if ret != 0:
      print "~ Error precompilando la aplicación"
      exit(ret)

   #
   modules = app.modules()
   classpath = app.getClasspath()

   # Si no existe la carpeta dist la crea
   dist_path = os.path.join(app.path, "dist")

   makeDirsIfNotExists(dist_path)

   fecha = date.today().isoformat()
   dest = os.path.join(dist_path, app.name() + fecha)

   path = {}
   path['app'] = os.path.join(dest, app.name())
   path['lib'] = os.path.join(dest, 'lib')
   path['modules'] = os.path.join(dest, app.name(),'modules')

   ignoreGlobal = ['**logs', '**test*', 'led', 'eclipse', 
                    '**tmp', '**test-result', 'modules', 
                    '.settings', '.classpath', 
                    'lib', 'nbproject', '**eclipse', '**.svn', '**.git']

   if os.path.exists(path['app']):
      print "~ [ERROR] - La carpeta de destino %s ya existe" % path['app']
      exit(1)
        
   print "~ Copiando aplicación a " + path['app']
   ignores = ignoreGlobal + ['dist']
   copytree(app.path, path['app'], ignores)

   # Copia las librerias de la aplicación y de los módulos
   # No las librerías de play
   print "~ Copiando librerías a" + path['lib']
   makeDirsIfNotExists(path['lib'])
   playlibs = os.path.join(app.play_env["basedir"], 'framework')
   for jar in classpath:
      if jar.endswith('.jar') and jar.find('provided-') == -1:
         if not jar.startswith(playlibs):
            jarname = os.path.split(jar)[1]
            shutil.copyfile(jar, os.path.join(path['lib'], jarname))
   
   #copiar módulos
   print "~ Copiando módulos" + path['modules']
   makeDirsIfNotExists(path['modules'])
   ignores = ignoreGlobal + ['dist', 'samples-and-tests', 'build.xml', 
              'documentation', '**compiler', 
              'plugins',  'fap-skel', 'src']

   for module in modules:
      modulename = os.path.basename(module)
      copytree(module, os.path.join(path['modules'], modulename), ignores)   



def replace_words(text, word_dic):
    rc = re.compile('|'.join(map(re.escape, word_dic)))
    def translate(match):
        return word_dic[match.group(0)]
    return rc.sub(translate, text)

def replace_in_file(file1, file2, word_dic):
    fin = open(file1, "r")
    str1 = fin.read()
    fin.close()

    str2 = replace_words(str1, word_dic)

    fout = open(file2, "w")
    fout.write(str2)
    fout.close()


def winservice(app, args):
    moduleDir = getModuleDir(app, args)
    winservicepath = os.path.join(moduleDir, "support", "winservice");

    prunsrv = os.path.join(winservicepath, 'commons-daemon-1.0.8-bin-windows', 'prunsrv.exe')

    install_in = os.path.join(winservicepath, "installService.bat")
    install_out = os.path.join(app.path, "installService.bat")
    word_dic = {
        "${app.name}" : app.name(),
        "${app.path}" : app.path,
        "${play.path}" : app.play_env['basedir'],
        "${prunsrv}" : prunsrv
    }
    replace_in_file(install_in, install_out, word_dic)
    print "~ [fap:winservice] Creado installService.bat"

    uninstall_in = os.path.join(winservicepath, "uninstallService.bat")
    uninstall_out = os.path.join(app.path, "uninstallService.bat")
    word_dic = {
        "${app.name}" : app.name(),
        "${prunsrv}" : prunsrv
    }
    replace_in_file(uninstall_in, uninstall_out, word_dic)
    print "~ [fap:winservice] Creado unistallService.bat"

def copyplatinoconf(app, args):
    fromPath = None
    try:
        optlist, args = getopt.getopt(args, '', ['from='])
        for o, a in optlist:
            if o in ('--from'):
                fromPath = a
    except getopt.GetoptError, err:
        print "~ %s" % str(err)
        
    if not fromPath:
        print "~ Especifica la carpeta donde está la configuracion de platino con --from"
        print "~ "
        sys.exit(-1)
    
    copytree_improved(fromPath, app.path, ignore=shutil.ignore_patterns('application.conf'))

    platinoApplicationConf = os.path.join(fromPath, "conf", "application.conf")
    appApplicationConf = os.path.join(app.path, "conf", "application.conf")
    append_content(platinoApplicationConf, appApplicationConf)

def copytree_improved(src, dst, symlinks=False, ignore=None):
    names = os.listdir(src)
    if ignore is not None:
        ignored_names = ignore(src, names)
    else:
        ignored_names = set()
    
    if not os.path.exists(dst):
        os.makedirs(dst)

    errors = []
    for name in names:
        if name in ignored_names:
            continue
        srcname = os.path.join(src, name)
        dstname = os.path.join(dst, name)

        if symlinks and os.path.islink(srcname):
            linkto = os.readlink(srcname)
            os.symlink(linkto, dstname)
        elif os.path.isdir(srcname):
            copytree_improved(srcname, dstname, symlinks, ignore)
        else:
            print "~ copy %s -> %s" % (srcname, dstname)
            shutil.copy2(srcname, dstname)

    shutil.copystat(src, dst)

def append_content(src, dst):
    if (os.path.exists(src)):
        print "~ append content %s -> %s" % (src, dst)

        from_f = open(src , 'r')
        from_content = from_f.read()
        from_f.close()
        
        to_f = open(dst, 'a')
        to_f.write(from_content)
        to_f.close() 
