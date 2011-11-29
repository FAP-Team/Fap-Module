#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os, os.path
import sys
import subprocess
import shutil
import re

# Here you can create play commands that are specific to the module, and extend existing commands

MODULE = 'fap'

# Commands that are specific to your module

COMMANDS = ['fap:hello', 'fap:generate', 'fap:init', 'fap:version', 'fap:documentation']
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

    
# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    if command == "dependencies":
        args.append('-Dfapsdk='+os.getenv("FAPSDK"))
        env = kargs.get("env")


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
    FILE.write("app.log.path=log4j-dev.properties\n"); 
    FILE.write("%prod.app.log.path=log4j-prod.properties\n");
    FILE.write("db=mem\n");
    FILE.write("%prod.jpa.ddl=create" + "\n");
    
    
    FILE.write("\n\n# === FAPGENERATED ===\n");
    FILE.write("# === END FAPGENERATED ===\n");
    FILE.close();
   

    log4jFile_a = os.path.join(moduleDir, "conf", "log4j-dev.properties");
    log4jFile_b = os.path.join(app.path, "conf", "log4j-dev.properties");
    shutil.copy2(log4jFile_a, log4jFile_b);

    log4jFile_a = os.path.join(moduleDir, "conf", "log4j-prod.properties");
    log4jFile_b = os.path.join(app.path, "conf", "log4j-prod.properties");
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
            destino = ruta_htmlDoc+"/"+f.replace(".fap", "FAPDocumentacion.html")
            # Por cada fichero ejecutamos la generacion de su documentacion
            classpath=ruta_clase+";"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\groovy-all-1.7.5.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-textile.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-wikitext.jar"
            cmd = [app.java_path(), "-Dfile.encoding=utf-8","-classpath", classpath, class_name, fuente, destino, ruta_plantilla, ruta_modulo.replace("\\", "/")];
            subprocess.call(cmd);
            print "~ [CREADO]: "+destino
    # Recorro la carpeta en busca de los fichero "*.fap", propios del proyecto
    ficheros = os.listdir(ruta_app)
    for f in ficheros:
        if (regexp.search(f)): # Si es un fichero "*.fap", creo su documentacion
            fuente = ruta_app+"\\"+f
            # Nombre del fichero destino de la documentacion
            destino = app.path.replace("\\", "/")+"/documentation/html"+"/"+f.replace(".fap", "Documentacion.html")
            # Por cada fichero ejecutamos la generacion de su documentacion
            classpath=ruta_clase+";"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\groovy-all-1.7.5.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-textile.jar;"+ruta_modulo+"\\compiler\\src\\es.fap.simpleled.generator\\lib\\jj-wikitext.jar"
            cmd = [app.java_path(), "-Dfile.encoding=utf-8","-classpath", classpath, class_name, fuente, destino, ruta_plantilla, ruta_modulo.replace("\\", "/")];
            subprocess.call(cmd);
            print "~ [CREADO]: "+destino

