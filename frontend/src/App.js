import './App.css';
import {QueryClient, QueryClientProvider} from 'react-query';
import { ReactQueryDevtools } from 'react-query/devtools';
import MovieComponent from './Movie';
import { ChakraProvider } from '@chakra-ui/react'

const queryClient = new QueryClient({});

function App() {
  return (
    <ChakraProvider>
      <QueryClientProvider client={queryClient}> 
        <MovieComponent/>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </ChakraProvider>
  );
}

export default App;
