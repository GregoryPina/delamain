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
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import com.gregorypina.delamain.domain.*
import com.gregorypina.delamain.integration.CompositeLocalActionPort
import com.gregorypina.delamain.integration.apps.AndroidLaunchAppActionPort
import com.gregorypina.delamain.integration.audio.*
import com.gregorypina.delamain.integration.system.AndroidBatteryStatusPort
import com.gregorypina.delamain.integration.voice.*
import kotlinx.coroutines.launch

private val DebugPanelBackground=Color(0xEE101820);private val DebugPanelAccent=Color(0xFF2E8BFF)
@Composable internal fun DebugCommandPanel(interactionCoordinator:InteractionCoordinator){
 val context=LocalContext.current.applicationContext;val scope=rememberCoroutineScope();var expanded by rememberSaveable{mutableStateOf(false)}
 val engine=remember(context){LocalCommandEngine(CompositeLocalActionPort(AndroidMediaVolumeActionPort.from(context),AndroidMediaKeyActionPort.from(context),AndroidLaunchAppActionPort.from(context)),AndroidBatteryStatusPort.from(context))}
 var outputPort by remember{mutableStateOf<AndroidTextToSpeechPort?>(null)};var selection by remember{mutableStateOf(SpeechVoiceSelection())};var speechState by remember{mutableStateOf(SpeechOutputState.Preparing)};var prefMessage by remember{mutableStateOf<String?>(null)};var saved by remember{mutableStateOf(false)};var activeSession by remember{mutableStateOf(0L)};var interactionId by remember{mutableStateOf(0L)}
 val store=remember(context){DataStoreSpeechVoicePreferenceStore(context)};val pref=remember(store){SpeechVoicePreferenceCoordinator(store){token,event->if(token==activeSession)when(event){is SpeechVoicePreferenceEvent.Restore->{val ok=outputPort?.restoreVoice(event.preference.engineId,event.preference.voiceId)==true;saved=ok;prefMessage=if(ok)"Voz salva restaurada." else "Preferência indisponível."};SpeechVoicePreferenceEvent.Saved->{saved=true;prefMessage="Preferência salva."};SpeechVoicePreferenceEvent.Cleared->{saved=false;prefMessage="Preferência removida."};SpeechVoicePreferenceEvent.SaveFailed->{saved=false;prefMessage="Usando nesta sessão; não foi possível salvar."};SpeechVoicePreferenceEvent.ClearFailed->{prefMessage="Não foi possível remover a preferência."};SpeechVoicePreferenceEvent.ReadFailed->{prefMessage="Não foi possível ler a preferência."};SpeechVoicePreferenceEvent.Unavailable->{saved=false;prefMessage="Preferência indisponível; usando padrão local."}}}}
 val owner=LocalView.current.findViewTreeLifecycleOwner();DisposableEffect(context,expanded,owner){if(expanded){val token=pref.openSession();activeSession=token;var restored=false
  val port=AndroidTextToSpeechPort(context,onState={speechState=it;interactionCoordinator.output(it,interactionId)},onVoices={s->selection=s;if(!restored&&s.engineId!=null&&s.ids.isNotEmpty()){restored=true;scope.launch{pref.restore(token,s.engineId!!,s.ids.toSet())}}});outputPort=port
  val observer=LifecycleEventObserver{_,e->if(e==Lifecycle.Event.ON_STOP){port.stop();interactionCoordinator.stop()}};owner?.lifecycle?.addObserver(observer);onDispose{owner?.lifecycle?.removeObserver(observer);pref.closeSession(token);port.shutdown();interactionCoordinator.stop();if(activeSession==token)outputPort=null}
 }else onDispose{}}
 Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding()){if(expanded)Content(engine,outputPort,speechState,selection,prefMessage,saved,interactionCoordinator,{interactionId=it},{id->val change=pref.beginExplicitChange();val ok=outputPort?.selectVoice(id)==true;val e=selection.engineId;saved=false;if(ok&&e!=null)scope.launch{pref.saveConfirmed(change,activeSession,e,id)};ok},{val change=pref.beginExplicitChange();val ok=outputPort?.selectDefaultVoice()==true;saved=false;if(ok)scope.launch{pref.clearPreference(change,activeSession)};ok},{outputPort?.stop();interactionCoordinator.stop();expanded=false},Modifier.align(Alignment.TopCenter))else TextButton({expanded=true},Modifier.align(Alignment.TopStart)){Text("DEV",color=DebugPanelAccent)}}
}
@Composable private fun Content(engine:LocalCommandEngine,out:SpeechOutputPort?,speechState:SpeechOutputState,selection:SpeechVoiceSelection,prefMessage:String?,saved:Boolean,coordinator:InteractionCoordinator,setInteraction:(Long)->Unit,onSelect:(String)->Boolean,onDefault:()->Boolean,onDismiss:()->Unit,modifier:Modifier){
 val focus=LocalFocusManager.current;var input by rememberSaveable{mutableStateOf("")};var output by rememberSaveable{mutableStateOf("Digite um comando local para testar.")};var problem by remember{mutableStateOf<String?>(null)};var inputPort by remember{mutableStateOf<SpeechInputPort?>(null)};var inputState by remember{mutableStateOf(SpeechInputState.Idle)};var iid by remember{mutableStateOf(0L)};val currentOut by rememberUpdatedState(out)
 fun newInteraction():Long=coordinator.begin().also{iid=it;setInteraction(it)}
 fun submit(text:String=input){inputPort?.cancel();val id=newInteraction();val pair=when(val r=engine.process(text)){is LocalCommandResult.Recognized->"${r.intent}: ${r.response}" to r.response;LocalCommandResult.Unknown->LocalUnknownResponses.next().let{it to it}};output=pair.first;problem=when(out?.speak(pair.second)){SpeechOutputResult.Queued->{coordinator.output(SpeechOutputState.Queued,id);null};SpeechOutputResult.Failed->{coordinator.error(id);"Não foi possível iniciar a fala."};else->{coordinator.error(id);"Voz indisponível; resposta em texto."}};focus.clearFocus()}
 val recognized by rememberUpdatedState<(String)->Unit>{input=it;submit(it)};val context=LocalContext.current.applicationContext;val owner=LocalView.current.findViewTreeLifecycleOwner();DisposableEffect(context,owner){val p=AndroidSpeechInputPort(context,{currentOut?.stop()?:false},{s->inputState=s;coordinator.input(s,iid)},{recognized(it)});inputPort=p;val o=LifecycleEventObserver{_,e->if(e==Lifecycle.Event.ON_STOP){p.cancel();coordinator.stop()}};owner?.lifecycle?.addObserver(o);onDispose{owner?.lifecycle?.removeObserver(o);p.shutdown();coordinator.stop();inputPort=null}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){inputState=if(it)SpeechInputState.Idle else SpeechInputState.PermissionRequired};val listening=inputState in setOf(SpeechInputState.Starting,SpeechInputState.Listening,SpeechInputState.Processing)
 Column(modifier.padding(12.dp).widthIn(max=620.dp).fillMaxWidth(.9f).heightIn(max=270.dp).background(DebugPanelBackground).verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row{Text("DESENVOLVIMENTO // COMANDO LOCAL",Modifier.weight(1f),color=DebugPanelAccent);TextButton({out?.stop();coordinator.stop()}){Text("PARAR VOZ")};TextButton({inputPort?.cancel();onDismiss()}){Text("FECHAR")}}
 Text(problem?:"Voz: $speechState",color=Color.White.copy(alpha=.7f),fontSize=12.sp);prefMessage?.let{Text("Preferência: $it",color=Color.White.copy(alpha=.6f),fontSize=11.sp)}
 if(selection.ids.isNotEmpty())Row{Text(if(saved)"Voz salva" else "Voz da sessão",Modifier.weight(1f));TextButton({inputPort?.cancel();selection.nextId()?.let{problem=if(onSelect(it))null else "Falha ao trocar voz"}}){Text("TROCAR VOZ")};TextButton({inputPort?.cancel();problem=if(onDefault())null else "Padrão indisponível; preferência mantida."}){Text("USAR PADRÃO")};TextButton({inputPort?.cancel();val id=newInteraction();problem=if(out?.speak("Olá. Sou a Vexa. Pronta para acompanhar sua viagem.")==SpeechOutputResult.Queued){coordinator.output(SpeechOutputState.Queued,id);null}else{coordinator.error(id);"Falha no exemplo"}}){Text("TESTAR VOZ")}}
 Row{TextButton(onClick={focus.clearFocus();if(listening){inputPort?.cancel();coordinator.stop()}else{if(currentOut?.stop()!=false){val id=newInteraction();if(inputPort?.start()==SpeechInputStartResult.PermissionRequired)permission.launch(Manifest.permission.RECORD_AUDIO);coordinator.input(SpeechInputState.Starting,id)}else problem="Não foi possível interromper a fala."}}){Text(if(listening)"CANCELAR ESCUTA" else "OUVIR")};Text(inputState.toString())}
 OutlinedTextField(input,{inputPort?.cancel();input=it},Modifier.fillMaxWidth(),label={Text("Entrada")},singleLine=true,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Send),keyboardActions=KeyboardActions(onSend={submit()}));Row{Button({submit()}){Text("ENVIAR")};Spacer(Modifier.width(12.dp));Text(output,color=Color.White,fontFamily=FontFamily.Monospace)}}
}
