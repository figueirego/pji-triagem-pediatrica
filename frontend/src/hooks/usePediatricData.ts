import { useCallback, useEffect, useState } from 'react';
import { getPediatricDemoData } from '../services/pediatricService';
import { riskContent } from '../mocks/appMock';
import type { PediatricDemoData } from '../types/domain';

const initialState: PediatricDemoData = {
  children: [],
  history: [],
  orientations: [],
  questions: [],
  risks: riskContent,
  symptoms: [],
};

interface PediatricDataState {
  data: PediatricDemoData;
  error: string | null;
  isLoading: boolean;
  reload: () => Promise<void>;
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : 'Não foi possível carregar os dados.';
}

export function usePediatricData(userId?: string | number | null): PediatricDataState {
  const [data, setData] = useState(initialState);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const nextData = await getPediatricDemoData(userId);
      setData(nextData);
    } catch (loadError: unknown) {
      setError(getErrorMessage(loadError));
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    data,
    error,
    isLoading,
    reload: load,
  };
}
