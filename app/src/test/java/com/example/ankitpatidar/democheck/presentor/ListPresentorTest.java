package com.example.ankitpatidar.democheck.presentor;

import com.example.ankitpatidar.democheck.interfaces.CallingInterface;
import com.example.ankitpatidar.democheck.model.ListItemHolder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;

import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;



@RunWith(PowerMockRunner.class)
@PrepareForTest({RetrofitUtil.class, ListPresentor.class})
public class ListPresentorTest {

    ListPresentor listPresentor;
    ListPresentor.DataFetcher dataFetcher;
    CallingInterface retrofitService;
    RetrofitUtil retrofitUtill;
    ArrayList<ListItemHolder> listItemHolders;
    
    
    private ArrayList<ListItemHolder> getListItemHolders(){
        ArrayList<ListItemHolder> list = new ArrayList<>();
        list.add(new ListItemHolder());
        list.add(new ListItemHolder());
        list.add(new ListItemHolder());
        list.add(new ListItemHolder());
        return list;
    }
       
    
    @Before
    public void setUp() throws Exception {
        dataFetcher = PowerMockito.mock(ListPresentor.DataFetcher.class);
        retrofitService = Mockito.mock(CallingInterface.class);
        retrofitUtill = Mockito.mock(RetrofitUtil.class);

        PowerMockito.whenNew(RetrofitUtil.class).withNoArguments().thenReturn(retrofitUtill);
        Mockito.when(retrofitUtill.getRetrofitService("http://starlord.hackerearth.com/")).thenReturn(retrofitService);
        
        listItemHolders = getListItemHolders();
        Call<List<ListItemHolder>> listCall = (Call<List<ListItemHolder>>) listItemHolders;
        Mockito.when(retrofitService.getListItemModel()).thenReturn(listCall);

        listPresentor = new ListPresentor(dataFetcher);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void fetchDataFromServer() throws Exception {

        listPresentor.fetchDataFromServer("http://starlord.hackerearth.com/");
        verify(dataFetcher.fetchDataFromServer(listItemHolders));
    }

}