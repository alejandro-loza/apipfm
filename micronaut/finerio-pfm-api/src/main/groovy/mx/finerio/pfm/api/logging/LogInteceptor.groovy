package mx.finerio.pfm.api.logging

import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.core.type.MutableArgumentValue

import java.lang.reflect.Method

import javax.inject.Singleton

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class LogInterceptor implements MethodInterceptor<Object, Object> {

  private final static Logger log = LoggerFactory.getLogger(
      LogInterceptor )

  @Override
  Object intercept( MethodInvocationContext<Object, Object> context ) {

    def fullName = getFullMethodName( context.targetMethod )
    log.info( "{} << {}", fullName,
        getParamsAsString( context.parameters ) )
    def returnValue = null

    try {
       returnValue = context.proceed()
    } catch( Exception e ) {
      log.info( "{} XX {} - {}", fullName,
          e.class.simpleName, e.message )
      throw e
    }

    log.info( "{} >> {}", fullName,
        returnValue != null ? returnValue : "VOID" )
    return returnValue

  }

  private String getFullMethodName( Method method ) {

    def clazz = method.declaringClass
    def className = clazz.simpleName
    def methodName = method.name
    return "${className}.${methodName}"

  }

  private String getParamsAsString(
      Map<String, MutableArgumentValue<?>> params ) {

    def paramsString = "OK"

    if ( !params.isEmpty() ) {
      paramsString = ""
    }

    for ( entry in params.entrySet() ) {

      def argument = entry.value
      def value = argument.value

      if ( argument.isAnnotationPresent( Hidden ) ) {
        value = "***HIDDEN***"
      }

      paramsString += "${entry.getKey()}: ${value}, "

    }

    return paramsString.size() > 2 ?
        paramsString.substring( 0, paramsString.size() - 2 ) :
        paramsString

  }

}
