package com.linbit.linstor.api.protobuf;

import com.linbit.ImplementationError;
import com.linbit.linstor.api.ApiCallRc;
import com.linbit.linstor.api.ApiCallRcImpl;
import com.linbit.linstor.proto.common.ApiCallResponseOuterClass;
import com.linbit.linstor.proto.common.LinStorMapEntryOuterClass;
import com.linbit.linstor.proto.common.ProviderTypeOuterClass.ProviderType;
import com.linbit.linstor.storage.kinds.DeviceProviderKind;
import com.linbit.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;

public class ProtoDeserializationUtils
{
    public static ApiCallRc.RcEntry parseApiCallRc(
        ApiCallResponseOuterClass.ApiCallResponse apiCallResponse,
        String messagePrefix
    )
    {
        ApiCallRcImpl.EntryBuilder entryBuilder = ApiCallRcImpl
            .entryBuilder(
                apiCallResponse.getRetCode(),
                messagePrefix + apiCallResponse.getMessage()
            );

        if (!StringUtils.isEmpty(apiCallResponse.getCause()))
        {
            entryBuilder.setCause(apiCallResponse.getCause());
        }
        if (!StringUtils.isEmpty(apiCallResponse.getCorrection()))
        {
            entryBuilder.setCorrection(apiCallResponse.getCorrection());
        }
        if (!StringUtils.isEmpty(apiCallResponse.getDetails()))
        {
            entryBuilder.setDetails(apiCallResponse.getDetails());
        }

        entryBuilder.putAllObjRefs(readLinStorMap(apiCallResponse.getObjRefsList()));

        entryBuilder.addAllErrorIds(apiCallResponse.getErrorReportIdsList());

        return entryBuilder.build();
    }

    private static Map<String, String> readLinStorMap(List<LinStorMapEntryOuterClass.LinStorMapEntry> linStorMap)
    {
        return linStorMap.stream()
            .collect(Collectors.toMap(
                LinStorMapEntryOuterClass.LinStorMapEntry::getKey,
                LinStorMapEntryOuterClass.LinStorMapEntry::getValue
            ));
    }

    public static byte[] extractByteArray(ByteString protoBytes)
    {
        byte[] arr = new byte[protoBytes.size()];
        protoBytes.copyTo(arr, 0);
        return arr;
    }

    public static DeviceProviderKind parseProviderKind(ProviderType providerKindRef)
    {
        DeviceProviderKind kind = null;
        if (providerKindRef != null)
        {
            switch (providerKindRef)
            {
                case DISKLESS:
                    kind = DeviceProviderKind.DISKLESS;
                    break;
                case LVM:
                    kind = DeviceProviderKind.LVM;
                    break;
                case LVM_THIN:
                    kind = DeviceProviderKind.LVM_THIN;
                    break;
                case SWORDFISH_INITIATOR:
                    kind = DeviceProviderKind.SWORDFISH_INITIATOR;
                    break;
                case SWORDFISH_TARGET:
                    kind = DeviceProviderKind.SWORDFISH_TARGET;
                    break;
                case ZFS:
                    kind = DeviceProviderKind.ZFS;
                    break;
                case ZFS_THIN:
                    kind = DeviceProviderKind.ZFS_THIN;
                    break;
                default:
                    throw new ImplementationError("Unknown (proto) ProviderType: " + providerKindRef);
            }
        }
        return kind;
    }

    private ProtoDeserializationUtils()
    {
    }
}
