package com.ivanasen.smarttickets.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.2.0.
 */
public class SmartTicketsCore extends Contract {
    private static final String BINARY = "0x606060405260008060146101000a81548160ff021916908315150217905550341561002957600080fd5b336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555033600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555033600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555033600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff021916908360ff160217905550611cf2806101946000396000f300606060405260043610610128576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680630519ce791461012d578063057ffcb31461018257806307cc9e7a146101b95780630a0f8168146101f05780631785f53c1461024557806327d7874c1461027e5780632ba73c15146102b75780633f4ba83a146102f05780634bfda2b1146103055780634e0a3379146103315780635c975abb1461036a57806367dd74ca146103975780636af04a57146103af5780636d1884e01461040457806370480275146104a757806371587988146104e0578063741c86c3146105195780637dc379fa1461056f5780638456cb59146105e45780638da5cb5b146105f9578063b047fb501461064e578063f2fde38b146106a3575b600080fd5b341561013857600080fd5b6101406106dc565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561018d57600080fd5b6101b760048080359060200190919080359060200190820180359060200191909192905050610702565b005b34156101c457600080fd5b6101ee600480803590602001909190803590602001908201803590602001919091929050506108a9565b005b34156101fb57600080fd5b61020361094d565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561025057600080fd5b61027c600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610973565b005b341561028957600080fd5b6102b5600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610a2b565b005b34156102c257600080fd5b6102ee600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b07565b005b34156102fb57600080fd5b610303610be3565b005b341561031057600080fd5b61032f6004808035906020019091908035906020019091905050610ca1565b005b341561033c57600080fd5b610368600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610d47565b005b341561037557600080fd5b61037d610e23565b604051808215151515815260200191505060405180910390f35b6103ad6004808035906020019091905050610e36565b005b34156103ba57600080fd5b6103c26110f4565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561040f57600080fd5b610425600480803590602001909190505061111a565b6040518083815260200180602001828103825283818151815260200191508051906020019080838360005b8381101561046b578082015181840152602081019050610450565b50505050905090810190601f1680156104985780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b34156104b257600080fd5b6104de600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919050506111f1565b005b34156104eb57600080fd5b610517600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919050506112e5565b005b341561052457600080fd5b61056d600480803590602001909190803590602001909190803561ffff169060200190919080359060200190919080359060200190919080351515906020019091905050611403565b005b341561057a57600080fd5b61059060048080359060200190919050506116c0565b604051808881526020018781526020018661ffff1661ffff1681526020018561ffff1661ffff1681526020018481526020018381526020018215151515815260200197505050505050505060405180910390f35b34156105ef57600080fd5b6105f7611751565b005b341561060457600080fd5b61060c611811565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561065957600080fd5b610661611836565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156106ae57600080fd5b6106da600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061185c565b005b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b61070a6119b1565b6000428511151561071a57600080fd5b604080519081016040528086815260200185858080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050815250915060016007805480600101828161077991906119d1565b916000526020600020906002020160008590919091506000820151816000015560208201518160010190805190602001906107b5929190611a03565b505050039050336008600083815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f263b8c982895fbe8f474fd5750a94f9bc7df584aad4f354ed2aef8c32ab8423c818686863360405180868152602001858152602001806020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182810382528585828181526020019250808284378201915050965050505050505060405180910390a15050505050565b823373ffffffffffffffffffffffffffffffffffffffff166008600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151561091757600080fd5b828260078681548110151561092857fe5b90600052602060002090600202016001019190610946929190611a83565b5050505050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156109cf57600080fd5b6000600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff021916908360ff16021790555050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610a8757600080fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614151515610ac357600080fd5b80600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610b6357600080fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614151515610b9f57600080fd5b80600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610c3e57600080fd5b600060149054906101000a900460ff161515610c5957600080fd5b60008060146101000a81548160ff0219169083151502179055507f7805862f689e2f13df9f062ff482ad3ad112aca9e0847911ed832e158c525b3360405160405180910390a1565b813373ffffffffffffffffffffffffffffffffffffffff166008600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141515610d0f57600080fd5b4282111515610d1d57600080fd5b81600784815481101515610d2d57fe5b906000526020600020906002020160000181905550505050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610da357600080fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614151515610ddf57600080fd5b80600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b600060149054906101000a900460ff1681565b6000600682815481101515610e4757fe5b90600052602060002090600602019050600073ffffffffffffffffffffffffffffffffffffffff166009600084815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151515610ec657600080fd5b4260078260000154815481101515610eda57fe5b906000526020600020906002020160000154111515610ef857600080fd5b60008160020160029054906101000a900461ffff1661ffff16111515610f1d57600080fd5b80600101543410151515610f3057600080fd5b6009600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc82600101549081150290604051600060405180830381858888f193505050501515610fa757600080fd5b3373ffffffffffffffffffffffffffffffffffffffff166108fc826001015434039081150290604051600060405180830381858888f193505050501515610fed57600080fd5b80600201600281819054906101000a900461ffff16809291906001900391906101000a81548161ffff021916908361ffff16021790555050600a60003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000838152602001908152602001600020600081548092919060010191905055507f23185667fbe4fe155ce70ec5978bcaaccf7b774f88cfb256ddef7d092c92e0218233604051808381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a15050565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000611124611b03565b600060078481548110151561113557fe5b9060005260206000209060020201905080600001549250806001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156111e45780601f106111b9576101008083540402835291602001916111e4565b820191906000526020600020905b8154815290600101906020018083116111c757829003601f168201915b5050505050915050915091565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561124d57600080fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff161415151561128957600080fd5b6001600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff021916908360ff16021790555050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561134157600080fd5b600060149054906101000a900460ff16151561135c57600080fd5b80600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f450db8da6efbe9c22f2347f7c2021231df1fc58d3ae9a2fa75d39fa44619930581604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a150565b61140b611b17565b6000873373ffffffffffffffffffffffffffffffffffffffff166008600083815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614151561147b57600080fd5b674563918244f40000881115151561149257600080fd5b60008761ffff161115156114a557600080fd5b42861115156114b357600080fd5b85851115156114c157600080fd5b60e0604051908101604052808a81526020018981526020018861ffff1681526020018861ffff16815260200187815260200186815260200185151581525092506001600680548060010182816115179190611b5f565b91600052602060002090600602016000869091909150600082015181600001556020820151816001015560408201518160020160006101000a81548161ffff021916908361ffff16021790555060608201518160020160026101000a81548161ffff021916908361ffff1602179055506080820151816003015560a0820151816004015560c08201518160050160006101000a81548160ff0219169083151502179055505050039150336009600084815260200190815260200160002060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f773dd92ace5d8ffae2cc2451a5c81548825b03482bdf8d1e82bc42cc757273f6828a8a8a8a8a8a33604051808981526020018881526020018781526020018661ffff168152602001858152602001848152602001831515151581526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019850505050505050505060405180910390a1505050505050505050565b6000806000806000806000806006898154811015156116db57fe5b9060005260206000209060060201905080600001549750806001015496508060020160009054906101000a900461ffff1695508060020160029054906101000a900461ffff16945080600301549350806004015492508060050160009054906101000a900460ff16915050919395979092949650565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156117ac57600080fd5b600060149054906101000a900460ff161515156117c857600080fd5b6001600060146101000a81548160ff0219169083151502179055507f6985a02210a168e66602d3235cb6db0e70f92b3ba4d376a33c0f3d9434bff62560405160405180910390a1565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156118b757600080fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16141515156118f357600080fd5b8073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a3806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b6040805190810160405280600081526020016119cb611b91565b81525090565b8154818355818115116119fe576002028160020283600052602060002091820191016119fd9190611ba5565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10611a4457805160ff1916838001178555611a72565b82800160010185558215611a72579182015b82811115611a71578251825591602001919060010190611a56565b5b509050611a7f9190611bdc565b5090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10611ac457803560ff1916838001178555611af2565b82800160010185558215611af2579182015b82811115611af1578235825591602001919060010190611ad6565b5b509050611aff9190611bdc565b5090565b602060405190810160405280600081525090565b60e0604051908101604052806000815260200160008152602001600061ffff168152602001600061ffff16815260200160008152602001600081526020016000151581525090565b815481835581811511611b8c57600602816006028360005260206000209182019101611b8b9190611c01565b5b505050565b602060405190810160405280600081525090565b611bd991905b80821115611bd557600080820160009055600182016000611bcc9190611c7e565b50600201611bab565b5090565b90565b611bfe91905b80821115611bfa576000816000905550600101611be2565b5090565b90565b611c7b91905b80821115611c775760008082016000905560018201600090556002820160006101000a81549061ffff02191690556002820160026101000a81549061ffff0219169055600382016000905560048201600090556005820160006101000a81549060ff021916905550600601611c07565b5090565b90565b50805460018160011615610100020316600290046000825580601f10611ca45750611cc3565b601f016020900490600052602060002090810190611cc29190611bdc565b5b505600a165627a7a72305820985ff2b9d807dab493abcdd572d3eec697bc9dad603e3de7588628045cbf43990029";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<>();
    }

    protected SmartTicketsCore(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SmartTicketsCore(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<EventCreationEventResponse> getEventCreationEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("EventCreation", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Address>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<EventCreationEventResponse> responses = new ArrayList<EventCreationEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            EventCreationEventResponse typedResponse = new EventCreationEventResponse();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.date = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.metaDescriptionHash = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.creator = (String) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<EventCreationEventResponse> eventCreationEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("EventCreation", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, EventCreationEventResponse>() {
            @Override
            public EventCreationEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                EventCreationEventResponse typedResponse = new EventCreationEventResponse();
                typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.date = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.metaDescriptionHash = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.creator = (String) eventValues.getNonIndexedValues().get(3).getValue();
                return typedResponse;
            }
        });
    }

    public List<EventCancelationEventResponse> getEventCancelationEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("EventCancelation", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<EventCancelationEventResponse> responses = new ArrayList<EventCancelationEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            EventCancelationEventResponse typedResponse = new EventCancelationEventResponse();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<EventCancelationEventResponse> eventCancelationEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("EventCancelation", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, EventCancelationEventResponse>() {
            @Override
            public EventCancelationEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                EventCancelationEventResponse typedResponse = new EventCancelationEventResponse();
                typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<TicketCreationEventResponse> getTicketCreationEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("TicketCreation", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}, new TypeReference<Address>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<TicketCreationEventResponse> responses = new ArrayList<TicketCreationEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            TicketCreationEventResponse typedResponse = new TicketCreationEventResponse();
            typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.eventId = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.supply = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.startVendingTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.endVendingTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.refundable = (Boolean) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.creator = (String) eventValues.getNonIndexedValues().get(7).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TicketCreationEventResponse> ticketCreationEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("TicketCreation", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}, new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TicketCreationEventResponse>() {
            @Override
            public TicketCreationEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                TicketCreationEventResponse typedResponse = new TicketCreationEventResponse();
                typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.eventId = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.supply = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.startVendingTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
                typedResponse.endVendingTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
                typedResponse.refundable = (Boolean) eventValues.getNonIndexedValues().get(6).getValue();
                typedResponse.creator = (String) eventValues.getNonIndexedValues().get(7).getValue();
                return typedResponse;
            }
        });
    }

    public List<TicketPurchaseEventResponse> getTicketPurchaseEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("TicketPurchase", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<TicketPurchaseEventResponse> responses = new ArrayList<TicketPurchaseEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            TicketPurchaseEventResponse typedResponse = new TicketPurchaseEventResponse();
            typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TicketPurchaseEventResponse> ticketPurchaseEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("TicketPurchase", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TicketPurchaseEventResponse>() {
            @Override
            public TicketPurchaseEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                TicketPurchaseEventResponse typedResponse = new TicketPurchaseEventResponse();
                typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.buyer = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<WithdrawalEventResponse> getWithdrawalEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Withdrawal", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<WithdrawalEventResponse> responses = new ArrayList<WithdrawalEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            WithdrawalEventResponse typedResponse = new WithdrawalEventResponse();
            typedResponse.to = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<WithdrawalEventResponse> withdrawalEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Withdrawal", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, WithdrawalEventResponse>() {
            @Override
            public WithdrawalEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                WithdrawalEventResponse typedResponse = new WithdrawalEventResponse();
                typedResponse.to = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<ContractUpgradeEventResponse> getContractUpgradeEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ContractUpgrade", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ContractUpgradeEventResponse> responses = new ArrayList<ContractUpgradeEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ContractUpgradeEventResponse typedResponse = new ContractUpgradeEventResponse();
            typedResponse.newContract = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ContractUpgradeEventResponse> contractUpgradeEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ContractUpgrade", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ContractUpgradeEventResponse>() {
            @Override
            public ContractUpgradeEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ContractUpgradeEventResponse typedResponse = new ContractUpgradeEventResponse();
                typedResponse.newContract = (String) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<PauseEventResponse> getPauseEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Pause", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<PauseEventResponse> responses = new ArrayList<PauseEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            PauseEventResponse typedResponse = new PauseEventResponse();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<PauseEventResponse> pauseEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Pause", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, PauseEventResponse>() {
            @Override
            public PauseEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                PauseEventResponse typedResponse = new PauseEventResponse();
                return typedResponse;
            }
        });
    }

    public List<UnpauseEventResponse> getUnpauseEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Unpause", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<UnpauseEventResponse> responses = new ArrayList<UnpauseEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            UnpauseEventResponse typedResponse = new UnpauseEventResponse();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<UnpauseEventResponse> unpauseEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Unpause", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, UnpauseEventResponse>() {
            @Override
            public UnpauseEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                UnpauseEventResponse typedResponse = new UnpauseEventResponse();
                return typedResponse;
            }
        });
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OwnershipTransferred", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OwnershipTransferred", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<String> cfoAddress() {
        Function function = new Function("cfoAddress", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> ceoAddress() {
        Function function = new Function("ceoAddress", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> removeAdmin(String _admin) {
        Function function = new Function(
                "removeAdmin", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_admin)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setCEO(String _newCEO) {
        Function function = new Function(
                "setCEO", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_newCEO)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setCOO(String _newCOO) {
        Function function = new Function(
                "setCOO", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_newCOO)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> unpause() {
        Function function = new Function(
                "unpause", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setCFO(String _newCFO) {
        Function function = new Function(
                "setCFO", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_newCFO)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> paused() {
        Function function = new Function("paused", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> newContractAddress() {
        Function function = new Function("newContractAddress", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addAdmin(String _newAdmin) {
        Function function = new Function(
                "addAdmin", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_newAdmin)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setNewAddress(String _v2Address) {
        Function function = new Function(
                "setNewAddress", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_v2Address)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> pause() {
        Function function = new Function(
                "pause", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> owner() {
        Function function = new Function("owner", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> cooAddress() {
        Function function = new Function("cooAddress", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> transferOwnership(String newOwner) {
        Function function = new Function(
                "transferOwnership", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<SmartTicketsCore> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SmartTicketsCore.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<SmartTicketsCore> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SmartTicketsCore.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public RemoteCall<TransactionReceipt> buyTicket(BigInteger _ticketId, BigInteger weiValue) {
        Function function = new Function(
                "buyTicket", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ticketId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<TransactionReceipt> createEvent(BigInteger _date, byte[] _metaDescriptionHash) {
        Function function = new Function(
                "createEvent", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_date), 
                new org.web3j.abi.datatypes.DynamicBytes(_metaDescriptionHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addTicketForEvent(BigInteger _eventId, BigInteger _priceInEther, BigInteger _initialSupply, BigInteger _startVendingTime, BigInteger _endVendingTime, Boolean _refundable) {
        Function function = new Function(
                "addTicketForEvent", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_eventId), 
                new org.web3j.abi.datatypes.generated.Uint256(_priceInEther), 
                new org.web3j.abi.datatypes.generated.Uint16(_initialSupply), 
                new org.web3j.abi.datatypes.generated.Uint256(_startVendingTime), 
                new org.web3j.abi.datatypes.generated.Uint256(_endVendingTime), 
                new org.web3j.abi.datatypes.Bool(_refundable)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setEventDate(BigInteger _id, BigInteger _date) {
        Function function = new Function(
                "setEventDate", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id), 
                new org.web3j.abi.datatypes.generated.Uint256(_date)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setEventMetaDescriptionHash(BigInteger _id, byte[] _hash) {
        Function function = new Function(
                "setEventMetaDescriptionHash", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id), 
                new org.web3j.abi.datatypes.DynamicBytes(_hash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple2<BigInteger, byte[]>> getEvent(BigInteger _id) {
        final Function function = new Function("getEvent", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}));
        return new RemoteCall<Tuple2<BigInteger, byte[]>>(
                new Callable<Tuple2<BigInteger, byte[]>>() {
                    @Override
                    public Tuple2<BigInteger, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);;
                        return new Tuple2<BigInteger, byte[]>(
                                (BigInteger) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, Boolean>> getTicket(BigInteger _ticketId) {
        final Function function = new Function("getTicket", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_ticketId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint16>() {}, new TypeReference<Uint16>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bool>() {}));
        return new RemoteCall<Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, Boolean>>(
                new Callable<Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, Boolean>>() {
                    @Override
                    public Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);;
                        return new Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, Boolean>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (Boolean) results.get(6).getValue());
                    }
                });
    }

    public static SmartTicketsCore load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SmartTicketsCore(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static SmartTicketsCore load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SmartTicketsCore(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class EventCreationEventResponse {
        public BigInteger id;

        public BigInteger date;

        public byte[] metaDescriptionHash;

        public String creator;
    }

    public static class EventCancelationEventResponse {
        public BigInteger id;
    }

    public static class TicketCreationEventResponse {
        public BigInteger ticketId;

        public BigInteger eventId;

        public BigInteger price;

        public BigInteger supply;

        public BigInteger startVendingTime;

        public BigInteger endVendingTime;

        public Boolean refundable;

        public String creator;
    }

    public static class TicketPurchaseEventResponse {
        public BigInteger ticketId;

        public String buyer;
    }

    public static class WithdrawalEventResponse {
        public BigInteger to;

        public BigInteger amount;
    }

    public static class ContractUpgradeEventResponse {
        public String newContract;
    }

    public static class PauseEventResponse {
    }

    public static class UnpauseEventResponse {
    }

    public static class OwnershipTransferredEventResponse {
        public String previousOwner;

        public String newOwner;
    }
}
