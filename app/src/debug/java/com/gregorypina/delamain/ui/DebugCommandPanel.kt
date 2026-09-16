package com.gregorypina.delamain.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.gregorypina.delamain.domain.*
import com.gregorypina.delamain.integration.CompositeLocalActionPort
import com.gregorypina.delamain.integration.apps.AndroidLaunchAppActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaKeyActionPort
import com.gregorypina.delamain.integration.audio.AndroidMediaVolumeActionPort
import com.gregorypina.delamain.integration.system.AndroidBatteryStatusPort
import com.gregorypina.delamain.integration.voice.*
import kotlinx.coroutines.launch

private val DebugPanelBackground=Color(0xEE101820); private val DebugPanelAccent=Color(0xFF2E8BFF)
@Composable internal fun DebugCommandPanel(){
 val context=LocalContext.current.applicationContext; val scope=rememberCoroutineScope(); var expanded by rememberSaveable{mutableStateOf(false)}
 val engine=remember(context){LocalCommandEngine(CompositeLocalActionPort(AndroidMediaVolumeActionPort.from(context),AndroidMediaKeyActionPort.from(context),AndroidLaunchAppActionPort.from(context)),AndroidBatteryStatusPort.from(context))}
 var outputPort by remember{mutableStateOf<AndroidTextToSpeechPort?>(null)}; var selection by remember{mutableStateOf(SpeechVoiceSelection())}; var speechState by remember{mutableStateOf(SpeechOutputState.Preparing)}
 var prefMessage by remember{mutableStateOf<String?>(null)}; var saved by remember{mutableStateOf(false)}; var activeSession by remember{mutableStateOf(0L)}
 val store=remember(context){DataStoreSpeechVoicePreferenceStore(context)}
 val coordinator=remember(store){SpeechVoicePreferenceCoordinator(store){token,event-> if(token==activeSession) when(event){
  is SpeechVoicePreferenceEvent.Restore->{val ok=outputPort?.restoreVoice(event.preference.engineId,event.preference.voiceId)==true;saved=ok;prefMessage=if(ok)"Voz salva restaurada." else "Preferência de voz indisponível; usando voz local padrão."}
  SpeechVoicePreferenceEvent.Unavailable->{saved=false;prefMessage="Preferência de voz indisponível; usando voz local padrão."}
  SpeechVoicePreferenceEvent.ReadFailed->{saved=false;prefMessage="Não foi possível ler a preferência; usando voz local nesta sessão."}
  SpeechVoicePreferenceEvent.Saved->{saved=true;prefMessage="Preferência de voz salva."}; SpeechVoicePreferenceEvent.SaveFailed->{saved=false;prefMessage="Usando nesta sessão; não foi possível salvar."}
  SpeechVoicePreferenceEvent.Cleared->{saved=false;prefMessage="Preferência removida; usando voz local padrão."}; SpeechVoicePreferenceEvent.ClearFailed->{prefMessage="Não foi possível remover a preferência salva."}
 }}}
 val owner=LocalView.current.findViewTreeLifecycleOwner()
 DisposableEffect(context,expanded,owner){if(expanded){speechState=SpeechOutputState.Preparing;selection=SpeechVoiceSelection();prefMessage=null;saved=false
  val token=coordinator.openSession();activeSession=token;var restored=false
  val port=AndroidTextToSpeechPort(context,onState={speechState=it},onVoices={s->selection=s;if(!restored&&s.engineId!=null&&s.ids.isNotEmpty()){restored=true;scope.launch{coordinator.restore(token,s.engineId!!,s.ids.toSet())}}});outputPort=port
  val observer=LifecycleEventObserver{_,e->if(e==Lifecycle.Event.ON_STOP)port.stop()};owner?.lifecycle?.addObserver(observer)
  onDispose{owner?.lifecycle?.removeObserver(observer);coordinator.closeSession(token);port.shutdown();if(activeSession==token)outputPort=null}
 }else onDispose{}}
 Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding()){if(expanded)DebugContent(engine,outputPort,speechState,selection,prefMessage,saved,
  onSelect={id->val change=coordinator.beginExplicitChange();val p=outputPort;val engineId=selection.engineId;val ok=p?.selectVoice(id)==true;saved=false;if(ok&&engineId!=null)scope.launch{coordinator.saveConfirmed(change,activeSession,engineId,id)};ok},
  onDefault={val change=coordinator.beginExplicitChange();val ok=outputPort?.selectDefaultVoice()==true;saved=false;if(ok)scope.launch{coordinator.clearPreference(change,activeSession)};ok},
  onDismiss={outputPort?.stop();expanded=false},Modifier.align(Alignment.TopCenter)) else TextButton({expanded=true},Modifier.align(Alignment.TopStart)){Text("DEV",color=DebugPanelAccent)}}
}
@Composable private fun DebugContent(engine:LocalCommandEngine,speechOutputPort:SpeechOutputPort?,speechState:SpeechOutputState,selection:SpeechVoiceSelection,prefMessage:String?,saved:Boolean,onSelect:(String)->Boolean,onDefault:()->Boolean,onDismiss:()->Unit,modifier:Modifier=Modifier){
 val focus=LocalFocusManager.current;var input by rememberSaveable{mutableStateOf("")};var output by rememberSaveable{mutableStateOf("Digite um comando local para testar.")};var problem by remember{mutableStateOf<String?>(null)};var inputPort by remember{mutableStateOf<SpeechInputPort?>(null)};var inputState by remember{mutableStateOf(SpeechInputState.Idle)};val currentOutput by rememberUpdatedState(speechOutputPort)
 fun submit(text:String=input){inputPort?.cancel();val pair=when(val r=engine.process(text)){is LocalCommandResult.Recognized->"${r.intent}: ${r.response}" to r.response;LocalCommandResult.Unknown->LocalUnknownResponses.next().let{it to it}};output=pair.first;problem=when(speechOutputPort?.speak(pair.second)){SpeechOutputResult.Queued->null;SpeechOutputResult.Failed->"Não foi possível iniciar a fala.";else->"Voz ainda não disponível; resposta em texto."};focus.clearFocus()}
 val recognized by rememberUpdatedState<(String)->Unit>{input=it;submit(it)};val context=LocalContext.current.applicationContext;val owner=LocalView.current.findViewTreeLifecycleOwner()
 DisposableEffect(context,owner){val p=AndroidSpeechInputPort(context,{currentOutput?.stop()?:false},{inputState=it},{recognized(it)});inputPort=p;val o=LifecycleEventObserver{_,e->if(e==Lifecycle.Event.ON_STOP)p.cancel()};owner?.lifecycle?.addObserver(o);onDispose{owner?.lifecycle?.removeObserver(o);p.shutdown();inputPort=null}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){inputState=if(it)SpeechInputState.Idle else SpeechInputState.PermissionRequired};val listening=inputState in setOf(SpeechInputState.Starting,SpeechInputState.Listening,SpeechInputState.Processing)
 Column(modifier.padding(12.dp).widthIn(max=620.dp).fillMaxWidth(.9f).heightIn(max=270.dp).background(DebugPanelBackground).verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
  Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text("DESENVOLVIMENTO // COMANDO LOCAL",color=DebugPanelAccent,fontSize=12.sp);TextButton({problem=null;speechOutputPort?.stop()}){Text("PARAR VOZ")};TextButton({inputPort?.cancel();onDismiss()}){Text("FECHAR")}}
  Text(problem?:when(speechState){SpeechOutputState.Preparing->"Voz: preparando…";SpeechOutputState.Ready->"Voz: pronta";SpeechOutputState.Unavailable->"Voz local pt-BR indisponível";SpeechOutputState.Queued->"Voz: aguardando início";SpeechOutputState.Speaking->"Voz: falando";SpeechOutputState.Completed->"Voz: fala concluída";SpeechOutputState.Stopped->"Voz: interrupção solicitada";SpeechOutputState.Failed->"Voz: falha; resposta em texto.";SpeechOutputState.Closed->"Voz: encerrada"},color=Color.White.copy(alpha=.7f),fontSize=12.sp)
  prefMessage?.let{Text("Preferência: $it",color=Color.White.copy(alpha=.6f),fontSize=11.sp)}
  if(selection.ids.isNotEmpty())Row(verticalAlignment=Alignment.CenterVertically){Text(if(selection.selectedId==null)"Voz não selecionada" else "Voz ${selection.ids.indexOf(selection.selectedId)+1} de ${selection.ids.size}"+if(saved)" (salva)" else " (sessão)",Modifier.weight(1f),color=Color.White.copy(alpha=.7f),fontSize=12.sp);TextButton({inputPort?.cancel();selection.nextId()?.let{problem=if(onSelect(it))null else "Não foi possível trocar a voz."}}){Text("TROCAR VOZ")};TextButton({inputPort?.cancel();problem=if(onDefault())null else "Não foi possível aplicar a voz local padrão; preferência mantida."}){Text("USAR PADRÃO")};TextButton({inputPort?.cancel();problem=when(speechOutputPort?.speak("Olá. Sou a Vexa. Pronta para acompanhar sua viagem.")){SpeechOutputResult.Queued->null;else->"Não foi possível reproduzir o exemplo."}}){Text("TESTAR VOZ")}}
  Row{TextButton(enabled=inputPort!=null&&speechOutputPort!=null,onClick={focus.clearFocus();if(listening)inputPort?.cancel() else if(inputPort?.start()==SpeechInputStartResult.PermissionRequired)permission.launch(Manifest.permission.RECORD_AUDIO)}){Text(if(listening)"CANCELAR ESCUTA" else "OUVIR")};Text(inputState.toString(),color=Color.White.copy(alpha=.7f),fontSize=12.sp)}
  OutlinedTextField(input,{inputPort?.cancel();input=it},Modifier.fillMaxWidth(),label={Text("Entrada")},singleLine=true,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Send),keyboardActions=KeyboardActions(onSend={submit()}))
  Row{Button({submit()}){Text("ENVIAR")};Spacer(Modifier.width(12.dp));Text(output,color=Color.White,fontFamily=FontFamily.Monospace)}
 }
}
